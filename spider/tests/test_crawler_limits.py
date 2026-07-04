import sys
import unittest
from pathlib import Path
from unittest.mock import patch


SPIDER_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(SPIDER_DIR))

from core import crawler  # noqa: E402


class CrawlerLimitTest(unittest.TestCase):
    @patch.object(crawler, "ensure_data_dirs")
    @patch.object(crawler, "ensure_state_db")
    def test_crawl_all_limits_each_category_across_pages(self, _state, _dirs):
        targets = [(1, "one", 3, 15), (2, "two", 3, 15)]

        def fake_crawl(tag, _name, page_num, page_size, limit):
            self.assertEqual(15, page_size)
            if page_num == 1:
                return []
            return [f"{tag}-{page_num}-{index}" for index in range(limit)]

        with (
            patch.object(crawler.settings, "CRAWL_TARGETS", targets),
            patch.object(crawler.settings, "CRAWL_MAX_NEW_PER_CATEGORY", 2),
            patch.object(crawler, "crawl_category", side_effect=fake_crawl) as crawl_category,
        ):
            results = crawler.crawl_all()

        self.assertEqual(4, len(results))
        self.assertEqual([1, 1, 2, 2], [call.args[0] for call in crawl_category.call_args_list])
        self.assertEqual([1, 2, 1, 2], [call.kwargs["page_num"] for call in crawl_category.call_args_list])

    @patch.object(crawler, "sleep_between_requests")
    @patch.object(crawler, "mark_post_crawled")
    @patch.object(crawler, "save_raw_item", return_value=Path("raw.json"))
    @patch.object(crawler, "raw_item_exists", return_value=False)
    @patch.object(crawler, "extract_source_department", return_value="source")
    @patch.object(crawler, "fetch_detail")
    @patch.object(crawler, "is_post_crawled", return_value=False)
    @patch.object(crawler, "fetch_posts_list")
    def test_crawl_category_stops_after_limit(
        self,
        fetch_posts,
        _is_crawled,
        fetch_detail,
        _extract_source,
        _raw_exists,
        save_raw,
        mark_crawled,
        sleep,
    ):
        fetch_posts.return_value = ([
            {"id": str(index), "title": f"title-{index}", "createTime": "2026-01-01"}
            for index in range(4)
        ], 4)
        fetch_detail.side_effect = lambda post_id: {
            "url": f"https://example.test/{post_id}",
            "title": f"detail-{post_id}",
            "content": "content",
        }

        with patch.object(crawler.settings, "ENABLE_STATE_DB", True):
            results = crawler.crawl_category(1, "one", limit=2)

        self.assertEqual(2, len(results))
        self.assertEqual(2, fetch_detail.call_count)
        self.assertEqual(2, save_raw.call_count)
        self.assertEqual(2, mark_crawled.call_count)
        self.assertEqual(2, sleep.call_count)


if __name__ == "__main__":
    unittest.main()
