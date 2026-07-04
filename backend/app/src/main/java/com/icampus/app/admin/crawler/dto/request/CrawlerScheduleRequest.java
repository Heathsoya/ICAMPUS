package com.icampus.app.admin.crawler.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CrawlerScheduleRequest {

    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @NotNull(message = "执行周期不能为空")
    @Min(value = 1, message = "执行周期不能少于1小时")
    @Max(value = 168, message = "执行周期不能超过168小时")
    private Integer intervalHours;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Integer getIntervalHours() { return intervalHours; }
    public void setIntervalHours(Integer intervalHours) { this.intervalHours = intervalHours; }
}
