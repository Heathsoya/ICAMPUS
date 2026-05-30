package com.icampus.api.dto.response;

/**
 * AuthService.login() 返回值
 */
public class LoginVO {

    private String token;
    private String role;
    private Long userId;
    private String username;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
