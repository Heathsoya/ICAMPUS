package com.icampus.app.qa.dto.response;

/**
 * 热点问题榜单项响应对象。
 *
 * 对应用例：
 * 查看热点问答榜单
 *
 * 用于向前端返回热点问题列表。
 *
 * 示例：
 * {
 *   "question": "宿舍几点关门？",
 *   "count": 156
 * }
 */
public class HotItemVO {

    /**
     * 热点问题标题。
     */
    private String question;

    /**
     * 热度值。
     *
     * 当前实现中使用问题出现次数统计：
     * count = 该问题在问答日志中的出现次数
     */
    private Long count;

    public HotItemVO() {
    }

    public HotItemVO(String question, Long count) {
        this.question = question;
        this.count = count;
    }

    /**
     * 静态构造方法。
     */
    public static HotItemVO of(String question, Long count) {
        return new HotItemVO(question, count);
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

    @Override
    public String toString() {
        return "HotItemVO{" +
                "question='" + question + '\'' +
                ", count=" + count +
                '}';
    }
}