package com.icampus.app.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AskRequest {

    @NotBlank(message = "问题内容不能为空")
    private String question;

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}
