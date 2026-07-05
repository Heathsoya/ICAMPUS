package com.icampus.app.admin.audit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AuditRequest {

    @NotNull(message = "贡献ID不能为空")
    private Long id;

    @NotBlank(message = "审核状态不能为空")
    private String status;

    private String reason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
