package com.icampus.infra.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("question_log")
public class QuestionLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String question;
    private String answer;
    private String matchedQuestion;
    private String category;
    private Double confidence;
    private String answerSource;
    private String relatedQuestions;
    private LocalDateTime createdAt;

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
