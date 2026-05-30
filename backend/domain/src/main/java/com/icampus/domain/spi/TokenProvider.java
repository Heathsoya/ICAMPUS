package com.icampus.domain.spi;

/**
 * Token 提供者 SPI 接口
 * <p>
 * 由 infra 模块提供 JWT 实现。
 * 定义在 domain 层，遵循依赖倒置原则。
 */
public interface TokenProvider {

    /**
     * 生成 Token
     */
    String generateToken(Long userId, String username, String role);

    /**
     * 校验 Token 是否有效
     */
    boolean validateToken(String token);

    /**
     * 从 Token 获取用户ID
     */
    Long getUserId(String token);

    /**
     * 从 Token 获取用户名
     */
    String getUsername(String token);

    /**
     * 从 Token 获取用户角色
     */
    String getRole(String token);
}
