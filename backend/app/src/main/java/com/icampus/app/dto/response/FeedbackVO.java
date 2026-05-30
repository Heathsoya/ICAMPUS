package com.icampus.app.dto.response;

/**
 * FeedbackService.submit() 返回值
 */
public class FeedbackVO {

    private Long id;
    private boolean recorded;

    public FeedbackVO() {}

    public FeedbackVO(Long id, boolean recorded) {
        this.id = id;
        this.recorded = recorded;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public boolean isRecorded() { return recorded; }
    public void setRecorded(boolean recorded) { this.recorded = recorded; }
}
