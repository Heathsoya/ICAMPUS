package com.icampus.app.qa.dto.request;

/**
 * 提问接口请求 DTO。
 * 
 * 接收用户输入的问题
 *
 * POST /api/qa/ask
 */
public class AskRequest {

    private String question;// 用户输入的问题

    private Long userId;// 当前用户ID
    
    public AskRequest() {
    }

    public AskRequest(String question, Long userId) {
        this.question = question;
        this.userId = userId;
    }

    public String getQuestion() {
        return question;
    }

    public Long getUserId() {
        return userId;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

