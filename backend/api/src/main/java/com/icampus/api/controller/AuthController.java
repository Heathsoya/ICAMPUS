package com.icampus.api.controller;

import com.icampus.api.dto.request.LoginRequest;
import com.icampus.api.dto.request.RegisterRequest;
import com.icampus.api.dto.response.IdStatusResponse;
import com.icampus.api.dto.response.LoginResponse;
import com.icampus.api.service.AuthService;
import com.icampus.core.ApiResponse;
import com.icampus.domain.entity.User;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 * <p>
 * 提供用户注册和登录功能。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户注册
     * <pre>
     * POST /api/auth/register
     * Body: { "username": "zhangsan", "password": "123456" }
     * → { "code": 200, "message": "success", "data": { "id": 1, "username": "zhangsan" } }
     * </pre>
     */
    @PostMapping("/register")
    public ApiResponse<IdStatusResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ApiResponse.success(new IdStatusResponse(user.getId(), user.getUsername()));
    }

    /**
     * 用户登录
     * <pre>
     * POST /api/auth/login
     * Body: { "username": "zhangsan", "password": "123456" }
     * → { "code": 200, "message": "success", "data": { "token": "eyJ...", "role": "USER" } }
     * </pre>
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }
}
