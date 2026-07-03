package com.icampus.app.dto.response;

/**
 * ContributionService.submit() 返回值
 */
public class ContributionVO {

    private Long id;
    private String status;

    public ContributionVO() {}

    public ContributionVO(Long id, String status) {
        this.id = id;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
