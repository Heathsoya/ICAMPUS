package com.icampus.api.service;

import com.icampus.api.dto.request.LoginRequest;
import com.icampus.api.dto.request.RegisterRequest;
import com.icampus.api.dto.response.LoginResponse;
import com.icampus.core.BusinessException;
import com.icampus.domain.entity.User;
import com.icampus.domain.repository.UserRepository;
import com.icampus.domain.spi.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户认证服务
 */
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(hashPassword(request.getPassword()));
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        log.info("新用户注册: {}", saved.getUsername());
        return saved;
    }

    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername().trim());
        if (userOpt.isEmpty()) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        User user = userOpt.get();
        if (!verifyPassword(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRole(user.getRole());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());

        log.info("用户登录成功: {}", user.getUsername());
        return response;
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    private String hashPassword(String plainPassword) {
        return "{plain}" + plainPassword;
    }

    private boolean verifyPassword(String plainPassword, String storedPassword) {
        return storedPassword.equals("{plain}" + plainPassword);
    }
}
