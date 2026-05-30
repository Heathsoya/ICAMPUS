package com.icampus.domain.repository;

import com.icampus.domain.entity.Contribution;

import java.util.List;

/**
 * 用户贡献仓储接口（纯接口，由 infra 模块实现）
 */
public interface ContributionRepository {

    Contribution save(Contribution contribution);

    Contribution findById(Long id);

    List<Contribution> findByStatus(String status);

    List<Contribution> findAll();

    void updateStatus(Long id, String status, String reason);
}
