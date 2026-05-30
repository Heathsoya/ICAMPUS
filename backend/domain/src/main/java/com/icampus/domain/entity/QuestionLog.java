package com.icampus.domain.entity;

import java.time.LocalDateTime;

/**
 * 问答日志实体（纯 POJO，零框架依赖）
 */
public class QuestionLog {

    private Long id;
    private Long userId;
    private String question;
    private String answer;
    private String matchedQuestion;
    private String category;
    private Double confidence;
    private String answerSource;
    private String relatedQuestions; // JSON 字符串
    private LocalDateTime createdAt;

    public QuestionLog() {}

    // ========== getter/setter ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getMatchedQuestion() { return matchedQuestion; }
    public void setMatchedQuestion(String matchedQuestion) { this.matchedQuestion = matchedQuestion; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public String getAnswerSource() { return answerSource; }
    public void setAnswerSource(String answerSource) { this.answerSource = answerSource; }
    public String getRelatedQuestions() { return relatedQuestions; }
    public void setRelatedQuestions(String relatedQuestions) { this.relatedQuestions = relatedQuestions; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
