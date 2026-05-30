package com.icampus.api.security;

import com.icampus.domain.spi.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * <p>
 * 从请求头 Authorization: Bearer <token> 中解析 JWT，
 * 验证通过后将用户信息写入 SecurityContext。
 * 不拦截未携带 Token 的请求（由 SecurityConfig 控制访问权限）。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    public JwtAuthenticationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());

            if (tokenProvider.validateToken(token)) {
                try {
                    Long userId = tokenProvider.getUserId(token);
                    String role = tokenProvider.getRole(token);

                    var authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + role));

                    var authentication = new UsernamePasswordAuthenticationToken(
                            userId, token, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT认证成功: userId={}, role={}", userId, role);
                } catch (Exception e) {
                    log.warn("JWT解析失败: {}", e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            } else {
                log.debug("JWT Token无效");
            }
        }

        filterChain.doFilter(request, response);
    }
}
