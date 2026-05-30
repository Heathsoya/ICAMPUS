package com.icampus.domain.spi;

/**
 * LLM 问题分析结果（纯 POJO）
 */
public class LlmAnalysis {

    /** 提取的关键词（空格分隔，用于全文检索） */
    private String keywords;

    /** 问题分类 */
    private String category;

    public LlmAnalysis() {}

    public LlmAnalysis(String keywords, String category) {
        this.keywords = keywords;
        this.category = category;
    }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
