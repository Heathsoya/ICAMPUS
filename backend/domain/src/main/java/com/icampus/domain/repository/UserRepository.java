package com.icampus.domain.repository;

import com.icampus.domain.entity.User;

import java.util.Optional;

/**
 * 用户仓储接口（纯接口，由 infra 模块实现）
 */
public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    User save(User user);

    boolean existsByUsername(String username);
}
