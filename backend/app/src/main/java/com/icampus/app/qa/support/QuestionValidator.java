package com.icampus.app.qa.support;

import org.springframework.stereotype.Component;

/**
 * 用户问题的清洗和校验
 */
@Component
public class QuestionValidator {

    private static final int MIN_LENGTH = 2;

    private static final int MAX_LENGTH = 255;

    /*
    * 去掉用户输入中的空格、换行符等
    */
    public String normalize(String question) {
        if (question == null) {
            return "";
        }

        return question.trim()
                .replaceAll("[\\t\\n\\r]", "")
                .replaceAll("\\s+", " ");
    }

    /**
     * 校验用户问题的合法性
     */
    public void validate(String question) {
        if (question == null || question.isBlank()) {
            throw new QaException("请输入您想咨询的问题");
        }

        if (question.length() < MIN_LENGTH) {
            throw new QaException("问题描述过短，请补充更多信息");
        }

        if (question.length() > MAX_LENGTH) {
            throw new QaException("问题过长，请控制在 255 字以内");
        }
    }
}
