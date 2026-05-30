package com.icampus.core;

/**
 * 业务异常
 * <p>
 * 在 Service 层抛出，由全局异常处理器统一捕获
 * 并转换为 {@link ApiResponse} 返回给前端。
 */
public class BusinessException extends RuntimeException {

    /** 业务错误码 */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
