package com.icampus.app.dto.response;

public class RelatedQuestion {

    private Long questionId;
    private String question;

    public RelatedQuestion() {}

    public RelatedQuestion(Long questionId, String question) {
        this.questionId = questionId;
        this.question = question;
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}
