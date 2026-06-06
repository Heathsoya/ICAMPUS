package com.icampus.app.qa.dto.response;

/**
 * 答案反馈响应 VO。
 *
 * 对应接口：
 * POST /api/qa/feedback
 *
 * 用于返回用户反馈是否成功记录。
 */
public class FeedbackVO {


    private Long feedbackId;// 反馈记录ID

    private Long questionLogId;// 关联的提问日志ID

    private Long knowledgeId;// 关联的知识库ID（如果答案来自知识库）

    private Long userId;// 当前用户ID

    private String feedbackType;// 反馈类型

    private Boolean recorded;// 是否成功记录反馈

    private String message;// 反馈结果消息

    public FeedbackVO() {
    }

    public FeedbackVO(Long feedbackId,
                      Long questionLogId,
                      Long knowledgeId,
                      Long userId,
                      String feedbackType,
                      Boolean recorded,
                      String message) {
        this.feedbackId = feedbackId;
        this.questionLogId = questionLogId;
        this.knowledgeId = knowledgeId;
        this.userId = userId;
        this.feedbackType = feedbackType;
        this.recorded = recorded;
        this.message = message;
    }


    public Long getFeedbackId() {
        return feedbackId;
    }

    public Long getQuestionLogId() {
        return questionLogId;
    }

    public Long getKnowledgeId() {
        return knowledgeId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public Boolean getRecorded() {
        return recorded;
    }

    public String getMessage() {
        return message;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public void setQuestionLogId(Long questionLogId) {
        this.questionLogId = questionLogId;
    }

    public void setKnowledgeId(Long knowledgeId) {
        this.knowledgeId = knowledgeId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }

    public void setRecorded(Boolean recorded) {
        this.recorded = recorded;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}