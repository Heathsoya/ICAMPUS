package com.icampus.app.dto.response;

/**
 * 管理后台知识库条目。
 */
public class KnowledgeItemVO {

    private Long id;
    private String question;
    private String category;
    private String keywords;
    private String source;
    private String updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
