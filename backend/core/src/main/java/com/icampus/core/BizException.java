package com.icampus.core;

/**
 * 业务异常
 * <p>
 * app 层同学只管 throw，api 层 GlobalExceptionHandler 统一 catch。
 * <pre>{@code
 * throw new BizException(BizCode.DUPLICATE_CONTENT);
 * throw new BizException(BizCode.SENSITIVE_WORD);
 * throw new BizException(BizCode.ALREADY_FEEDBACK);
 * }</pre>
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(BizCode bizCode) {
        super(bizCode.getMessage());
        this.code = bizCode.getCode();
    }

    public BizException(BizCode bizCode, String detail) {
        super(detail != null ? detail : bizCode.getMessage());
        this.code = bizCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
