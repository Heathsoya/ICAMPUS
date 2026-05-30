package com.icampus.api.dto.request;

import jakarta.validation.constraints.NotNull;

public class FeedbackRequest {

    @NotNull(message = "问答日志ID不能为空")
    private Long questionLogId;

    @NotNull(message = "反馈评价不能为空")
    private Boolean helpful;

    private String comment;

    public Long getQuestionLogId() { return questionLogId; }
    public void setQuestionLogId(Long questionLogId) { this.questionLogId = questionLogId; }
    public Boolean getHelpful() { return helpful; }
    public void setHelpful(Boolean helpful) { this.helpful = helpful; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
