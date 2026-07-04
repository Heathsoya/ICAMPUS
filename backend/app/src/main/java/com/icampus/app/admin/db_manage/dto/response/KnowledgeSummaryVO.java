package com.icampus.app.admin.db_manage.dto.response;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理后台知识库统计和列表。
 */
public class KnowledgeSummaryVO {

    private long total;
    private long crawlerCount;
    private List<KnowledgeItemVO> items = new ArrayList<>();

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public long getCrawlerCount() { return crawlerCount; }
    public void setCrawlerCount(long crawlerCount) { this.crawlerCount = crawlerCount; }
    public List<KnowledgeItemVO> getItems() { return items; }
    public void setItems(List<KnowledgeItemVO> items) { this.items = items; }
}
