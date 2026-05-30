package com.icampus.infra.repository.impl;

import com.icampus.domain.entity.User;
import com.icampus.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户仓储 — 内存实现
 * <p>
 * 开发阶段使用，后续替换为 MyBatis-Plus Mapper 实现。
 */
public class InMemoryUserRepository implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(InMemoryUserRepository.class);

    private final Map<Long, User> store = new ConcurrentHashMap<>();
    private final Map<String, User> usernameIndex = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(usernameIndex.get(username));
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        store.put(user.getId(), user);
        usernameIndex.put(user.getUsername(), user);
        log.debug("保存用户: id={}, username={}", user.getId(), user.getUsername());
        return user;
    }

    @Override
    public boolean existsByUsername(String username) {
        return usernameIndex.containsKey(username);
    }
}
