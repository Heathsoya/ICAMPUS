package com.icampus.app.qa.dto.response;

import java.util.ArrayList;
import java.util.List;

/**
 * 提问接口返回 VO。
 */
public class AskVO {

    private String answer;

    private String matchedQuestion;

    private Long matchedKnowledgeId;

    private String category;

    private Double confidence;

    private List<String> relatedQuestions = new ArrayList<>();

    private Long questionLogId;

    /**
     * KNOWLEDGE_BASE / LLM / NOT_FOUND
     */
    private String answerSource;

    /**
     * 是否调用了大模型。
     */
    private Boolean llmUsed;

    public String getAnswer() {
        return answer;
    }

    public String getMatchedQuestion() {
        return matchedQuestion;
    }

    public Long getMatchedKnowledgeId() {
        return matchedKnowledgeId;
    }

    public String getCategory() {
        return category;
    }

    public Double getConfidence() {
        return confidence;
    }

    public List<String> getRelatedQuestions() {
        return relatedQuestions;
    }

    public Long getQuestionLogId() {
        return questionLogId;
    }

    public String getAnswerSource() {
        return answerSource;
    }

    public Boolean getLlmUsed() {
        return llmUsed;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setMatchedQuestion(String matchedQuestion) {
        this.matchedQuestion = matchedQuestion;
    }

    public void setMatchedKnowledgeId(Long matchedKnowledgeId) {
        this.matchedKnowledgeId = matchedKnowledgeId;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public void setRelatedQuestions(List<String> relatedQuestions) {
        this.relatedQuestions = relatedQuestions;
    }

    public void setQuestionLogId(Long questionLogId) {
        this.questionLogId = questionLogId;
    }

    public void setAnswerSource(String answerSource) {
        this.answerSource = answerSource;
    }

    public void setLlmUsed(Boolean llmUsed) {
        this.llmUsed = llmUsed;
    }
}