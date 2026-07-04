package com.icampus.domain.model;

import java.time.LocalDateTime;

/**
 * 爬虫任务运行状态。
 */
public class CrawlerJobStatus {

    private boolean available;
    private boolean running;
    private boolean enabled;
    private int intervalHours;
    private LocalDateTime nextRunAt;
    private LocalDateTime lastStartedAt;
    private LocalDateTime lastFinishedAt;
    private Integer lastExitCode;
    private String message;

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public boolean isRunning() { return running; }
    public void setRunning(boolean running) { this.running = running; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getIntervalHours() { return intervalHours; }
    public void setIntervalHours(int intervalHours) { this.intervalHours = intervalHours; }
    public LocalDateTime getNextRunAt() { return nextRunAt; }
    public void setNextRunAt(LocalDateTime nextRunAt) { this.nextRunAt = nextRunAt; }
    public LocalDateTime getLastStartedAt() { return lastStartedAt; }
    public void setLastStartedAt(LocalDateTime lastStartedAt) { this.lastStartedAt = lastStartedAt; }
    public LocalDateTime getLastFinishedAt() { return lastFinishedAt; }
    public void setLastFinishedAt(LocalDateTime lastFinishedAt) { this.lastFinishedAt = lastFinishedAt; }
    public Integer getLastExitCode() { return lastExitCode; }
    public void setLastExitCode(Integer lastExitCode) { this.lastExitCode = lastExitCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
