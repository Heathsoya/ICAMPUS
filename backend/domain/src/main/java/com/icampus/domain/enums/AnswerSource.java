package com.icampus.domain.enums;

/**
 * 答案来源枚举
 */
public enum AnswerSource {

    /** 命中知识库 */
    KNOWLEDGE_BASE("knowledge_base"),

    /** 未命中 */
    NOT_FOUND("not_found"),

    /** 大模型生成 */
    LLM("llm");

    private final String value;

    AnswerSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
