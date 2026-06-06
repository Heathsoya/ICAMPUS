package com.icampus.app.qa.dto.request;

/**
 * 答案反馈请求 DTO。
 * 
 * 接收用户对答案的反馈信息，包括：
 *
 * POST /api/qa/feedback
 */
public class FeedbackRequest {

    private Long questionLogId;// 关联的提问日志ID

    private Long knowledgeId;// 关联的知识库ID（如果答案来自知识库）

    private Long userId;// 当前用户ID

    /**
     * USEFUL / USELESS
     */
    private String feedbackType;// 反馈类型

    private String feedbackContent;// 反馈内容

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

    public String getFeedbackContent() {
        return feedbackContent;
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

    public void setFeedbackContent(String feedbackContent) {
        this.feedbackContent = feedbackContent;
    }
}