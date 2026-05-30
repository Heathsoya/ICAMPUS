package com.icampus.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当前用户ID注解
 * <p>
 * 用于 Controller 方法参数，自动注入当前登录用户ID。
 * <pre>{@code
 * @PostMapping("/ask")
 * public ApiResponse<AskResponse> ask(@Valid @RequestBody AskRequest request,
 *                                      @CurrentUserId Long userId) { ... }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}
