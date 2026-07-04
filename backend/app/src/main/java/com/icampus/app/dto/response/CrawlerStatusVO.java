package com.icampus.app.dto.response;

public class CrawlerStatusVO {

    private boolean available;
    private boolean running;
    private boolean enabled;
    private int intervalHours;
    private String nextRunAt;
    private String lastStartedAt;
    private String lastFinishedAt;
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
    public String getNextRunAt() { return nextRunAt; }
    public void setNextRunAt(String nextRunAt) { this.nextRunAt = nextRunAt; }
    public String getLastStartedAt() { return lastStartedAt; }
    public void setLastStartedAt(String lastStartedAt) { this.lastStartedAt = lastStartedAt; }
    public String getLastFinishedAt() { return lastFinishedAt; }
    public void setLastFinishedAt(String lastFinishedAt) { this.lastFinishedAt = lastFinishedAt; }
    public Integer getLastExitCode() { return lastExitCode; }
    public void setLastExitCode(Integer lastExitCode) { this.lastExitCode = lastExitCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
