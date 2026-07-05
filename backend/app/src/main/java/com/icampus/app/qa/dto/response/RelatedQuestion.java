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

    /**
     * 相关问题 ID。
     *
     * 通常对应知识库中的问题 ID 或日志中的问题 ID。
     */
    private Long questionId;

    /**
     * 相关问题内容。
     */
    private String question;

    public RelatedQuestion() {
    }

    public RelatedQuestion(Long questionId, String question) {
        this.questionId = questionId;
        this.question = question;
    }

    public static RelatedQuestion of(Long questionId, String question) {
        return new RelatedQuestion(questionId, question);
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

    @Override
    public String toString() {
        return "RelatedQuestion{" +
                "questionId=" + questionId +
                ", question='" + question + '\'' +
                '}';
    }
}
