package com.icampus.api.service;

import com.icampus.api.dto.request.LoginRequest;
import com.icampus.api.dto.request.RegisterRequest;
import com.icampus.api.dto.response.LoginVO;
import com.icampus.api.dto.response.RegisterVO;
import com.icampus.core.BizCode;
import com.icampus.core.BizException;
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

    public RegisterVO register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BizException(BizCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(hashPassword(request.getPassword()));
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        log.info("新用户注册: {}", saved.getUsername());
        return new RegisterVO(saved.getId(), saved.getUsername());
    }

    public LoginVO login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername().trim());
        if (userOpt.isEmpty()) {
            throw new BizException(BizCode.LOGIN_FAILED);
        }

        User user = userOpt.get();
        if (!verifyPassword(request.getPassword(), user.getPassword())) {
            throw new BizException(BizCode.LOGIN_FAILED);
        }

        String token = tokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setRole(user.getRole());
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());

        log.info("用户登录成功: {}", user.getUsername());
        return vo;
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BizException(BizCode.NOT_FOUND));
    }

    private String hashPassword(String plainPassword) {
        return "{plain}" + plainPassword;
    }

    private boolean verifyPassword(String plainPassword, String storedPassword) {
        return storedPassword.equals("{plain}" + plainPassword);
    }
}
