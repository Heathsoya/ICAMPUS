import sys
import unittest
from pathlib import Path
from unittest.mock import patch


SPIDER_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(SPIDER_DIR))

import run_process_only  # noqa: E402


class ProcessLimitTest(unittest.TestCase):
    def test_process_stops_at_run_qa_limit(self):
        raw_items = [
            {"post_id": str(index), "url": f"https://example.test/{index}", "content": "content"}
            for index in range(20)
        ]
        processed = [
            {"question": f"q-{index}", "answer": "a", "category": "c", "keywords": ["k"]}
            for index in range(3)
        ]

        with (
            patch.object(run_process_only, "setup_logging"),
            patch.object(run_process_only, "load_raw_items", return_value=raw_items),
            patch.object(run_process_only, "processed_item_exists", return_value=False),
            patch.object(run_process_only, "should_skip", return_value=(False, "")),
            patch.object(run_process_only, "clean_text", side_effect=lambda value: value),
            patch.object(run_process_only, "process_raw_item", return_value=processed) as process,
            patch.object(run_process_only, "validate_processed_item", return_value=(True, "")),
            patch.object(run_process_only, "export_to_csv", return_value=Path("knowledge.csv")) as export,
            patch.object(run_process_only, "export_db_csv"),
            patch.object(run_process_only.settings, "MYSQL_IMPORT_ENABLED", False),
            patch.object(run_process_only.settings, "LLM_MAX_QA_PER_ANNOUNCEMENT", 3),
            patch.object(run_process_only.settings, "CRAWL_MAX_QA_PER_RUN", 30),
        ):
            run_process_only.main()

        self.assertEqual(10, process.call_count)
        self.assertEqual(30, len(export.call_args.args[0]))


if __name__ == "__main__":
    unittest.main()
