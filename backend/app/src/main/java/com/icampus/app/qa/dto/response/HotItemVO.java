package com.icampus.app.qa.dto.response;

/**
 * 热点问题榜单项响应对象。
 *
 * QnaService.getHotList() 或 QnaService.getHotQuestions() 返回值中的单个元素。
 *
 * 示例：
 * {
 *   "question": "宿舍几点关门？",
 *   "count": 156
 * }
 */
public class HotItemVO {


    private String question;// 热点问题

    private Long count;// 提问次数

    public HotItemVO() {
    }

    public HotItemVO(String question, Long count) {
        this.question = question;
        this.count = count;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}