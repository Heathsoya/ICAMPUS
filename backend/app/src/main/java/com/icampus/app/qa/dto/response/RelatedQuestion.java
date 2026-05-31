package com.icampus.app.qa.dto.response;

/**
 * 相关问题响应对象。
 *
 * 用于智能问答返回结果中的 relatedQuestions 字段。
 *
 * 示例：
 * {
 *   "questionId": 2,
 *   "question": "宿舍门禁时间是什么？"
 * }
 */
public class RelatedQuestion {


    private Long questionId;// 相关问题ID

    private String question;// 相关问题

    public RelatedQuestion() {
    }

    public RelatedQuestion(Long questionId, String question) {
        this.questionId = questionId;
        this.question = question;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}