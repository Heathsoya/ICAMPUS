package com.icampus.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.icampus.domain.entity.User;
import com.icampus.domain.repository.UserRepository;
import com.icampus.infra.persistence.entity.UserDO;
import com.icampus.infra.persistence.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MysqlUserRepository implements UserRepository {

    private final UserMapper userMapper;

    public MysqlUserRepository(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(toDomain(userMapper.selectById(id)));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, username);
        return Optional.ofNullable(toDomain(userMapper.selectOne(wrapper)));
    }

    @Override
    public User save(User user) {
        UserDO data = toData(user);
        if (data.getId() == null) {
            userMapper.insert(data);
        } else {
            userMapper.updateById(data);
        }
        user.setId(data.getId());
        return user;
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, username);
        return userMapper.selectCount(wrapper) > 0;
    }

    private UserDO toData(User user) {
        if (user == null) {
            return null;
        }
        UserDO data = new UserDO();
        data.setId(user.getId());
        data.setUsername(user.getUsername());
        data.setPassword(user.getPassword());
        data.setRole(user.getRole());
        data.setCreatedAt(user.getCreatedAt());
        data.setUpdatedAt(user.getUpdatedAt());
        return data;
    }

    private User toDomain(UserDO data) {
        if (data == null) {
            return null;
        }
        User user = new User();
        user.setId(data.getId());
        user.setUsername(data.getUsername());
        user.setPassword(data.getPassword());
        user.setRole(data.getRole());
        user.setCreatedAt(data.getCreatedAt());
        user.setUpdatedAt(data.getUpdatedAt());
        return user;
    }
}
