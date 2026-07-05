package com.icampus.app.admin.audit.dto.response;

/**
 * ContributionService.getAuditList() 返回值
 */
public class AuditItemVO {

    private Long id;
    private String question;
    private String answer;
    private String status;
    private String submitter;
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSubmitter() { return submitter; }
    public void setSubmitter(String submitter) { this.submitter = submitter; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
