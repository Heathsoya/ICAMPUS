package com.icampus.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 * <p>
 * 接口权限规则：
 * <ul>
 *   <li>/api/auth/**      — 公开访问（登录/注册）</li>
 *   <li>/api/qna/ask      — 公开访问（允许匿名提问）</li>
 *   <li>/api/qna/hot      — 公开访问（热点榜单）</li>
 *   <li>/api/qna/feedback — 需登录</li>
 *   <li>/api/contribution/** — 需登录</li>
 *   <li>/api/admin/**     — 需 ADMIN 角色</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（API 服务用 JWT，不需要 CSRF）
            .csrf(csrf -> csrf.disable())

            // 无状态会话
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 接口权限配置
            .authorizeHttpRequests(auth -> auth
                    // 公开接口
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/qna/hot").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/qna/ask").permitAll()

                    // 前端静态页面
                    .requestMatchers("/", "/index.html", "/style.css", "/script.js", "/*.ico", "/error").permitAll()

                    // 管理员接口
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // 其余需要认证
                    .anyRequest().authenticated()
            )

            // JWT 过滤器插入到 UsernamePasswordAuthenticationFilter 之前
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
