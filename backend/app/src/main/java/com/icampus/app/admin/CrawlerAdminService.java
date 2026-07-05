package com.icampus.app.admin;

import com.icampus.app.admin.crawler.dto.request.CrawlerScheduleRequest;
import com.icampus.app.admin.crawler.dto.response.CrawlerStatusVO;
import com.icampus.domain.model.CrawlerJobStatus;
import com.icampus.domain.spi.CrawlerManager;

import java.time.LocalDateTime;

public class CrawlerAdminService {

    private final CrawlerManager crawlerManager;

    public CrawlerAdminService(CrawlerManager crawlerManager) {
        this.crawlerManager = crawlerManager;
    }

    public CrawlerStatusVO getStatus() {
        return toVO(crawlerManager.getStatus());
    }

    public CrawlerStatusVO trigger() {
        return toVO(crawlerManager.trigger());
    }

    public CrawlerStatusVO configure(CrawlerScheduleRequest request) {
        return toVO(crawlerManager.configure(request.getEnabled(), request.getIntervalHours()));
    }

    private CrawlerStatusVO toVO(CrawlerJobStatus status) {
        CrawlerStatusVO vo = new CrawlerStatusVO();
        vo.setAvailable(status.isAvailable());
        vo.setRunning(status.isRunning());
        vo.setEnabled(status.isEnabled());
        vo.setIntervalHours(status.getIntervalHours());
        vo.setNextRunAt(format(status.getNextRunAt()));
        vo.setLastStartedAt(format(status.getLastStartedAt()));
        vo.setLastFinishedAt(format(status.getLastFinishedAt()));
        vo.setLastExitCode(status.getLastExitCode());
        vo.setMessage(status.getMessage());
        return vo;
    }

    private String format(LocalDateTime value) {
        return value == null ? null : value.toString();
    }
}
