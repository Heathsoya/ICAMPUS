package com.icampus.app.qa.support;
/**
 * QA模块自定义异常类
 */
public class QaException extends RuntimeException {

    public QaException(String message) {
        super(message);
    }
}