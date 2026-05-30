package com.icampus.api.dto.response;

/**
 * QnaService.getHotList() 返回值
 */
public class HotItemVO {

    private String question;
    private Long count;

    public HotItemVO() {}

    public HotItemVO(String question, Long count) {
        this.question = question;
        this.count = count;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}
