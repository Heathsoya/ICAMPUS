package com.icampus.domain.spi;

import com.icampus.domain.model.CrawlerJobStatus;

/**
 * 爬虫任务管理接口，由基础设施层负责执行本地爬虫进程。
 */
public interface CrawlerManager {

    CrawlerJobStatus getStatus();

    CrawlerJobStatus trigger();

    CrawlerJobStatus configure(boolean enabled, int intervalHours);
}
