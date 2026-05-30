package com.icampus.api.dto.response;

import java.util.List;

public class AskResponse {

    private String answer;
    private String matchedQuestion;
    private String category;
    private Double confidence;
    private String answerSource;
    private List<RelatedQuestion> relatedQuestions;
    private Long questionLogId;

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
    public List<RelatedQuestion> getRelatedQuestions() { return relatedQuestions; }
    public void setRelatedQuestions(List<RelatedQuestion> relatedQuestions) { this.relatedQuestions = relatedQuestions; }
    public Long getQuestionLogId() { return questionLogId; }
    public void setQuestionLogId(Long questionLogId) { this.questionLogId = questionLogId; }
}
