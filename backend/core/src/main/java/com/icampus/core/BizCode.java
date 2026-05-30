package com.icampus.core;

/**
 * 业务错误码枚举
 * <p>
 * app 层同学直接用：throw new BizException(BizCode.DUPLICATE_CONTENT)
 * api 层 GlobalExceptionHandler 统一 catch 转 JSON。
 */
public enum BizCode {

    // ====== 通用 ======
    OK(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ====== 业务 ======
    DUPLICATE_CONTENT(1001, "内容重复，已有相似问题"),
    SENSITIVE_WORD(1002, "内容包含敏感词"),
    ALREADY_FEEDBACK(1003, "已提交过反馈，不可重复提交"),
    USERNAME_EXISTS(1004, "用户名已被注册"),
    LOGIN_FAILED(1005, "用户名或密码错误"),
    QUESTION_LOG_NOT_FOUND(1006, "问答日志不存在"),
    CONTRIBUTION_NOT_FOUND(1007, "贡献记录不存在"),
    INVALID_AUDIT_STATUS(1008, "无效的审核状态"),
    LLM_TIMEOUT(1009, "大模型调用超时，请稍后重试"),
    ;

    private final int code;
    private final String message;

    BizCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
