package com.icampus.app.dto.response;

/**
 * AuthService.register() 返回值
 */
public class RegisterVO {

    private Long id;
    private String username;

    public RegisterVO() {}

    public RegisterVO(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
