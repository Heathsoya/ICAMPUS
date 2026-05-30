package com.icampus.infra.repository.impl;

import com.icampus.domain.entity.Contribution;
import com.icampus.domain.repository.ContributionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 贡献仓储 — 内存实现
 */
public class InMemoryContributionRepository implements ContributionRepository {

    private static final Logger log = LoggerFactory.getLogger(InMemoryContributionRepository.class);

    private final Map<Long, Contribution> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Contribution save(Contribution contribution) {
        if (contribution.getId() == null) {
            contribution.setId(idGenerator.getAndIncrement());
        }
        store.put(contribution.getId(), contribution);
        log.debug("保存贡献: id={}, question={}", contribution.getId(), contribution.getQuestion());
        return contribution;
    }

    @Override
    public Contribution findById(Long id) {
        return store.get(id);
    }

    @Override
    public List<Contribution> findByStatus(String status) {
        return store.values().stream()
                .filter(c -> status.equals(c.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Contribution> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void updateStatus(Long id, String status, String reason) {
        Contribution c = store.get(id);
        if (c != null) {
            c.setStatus(status);
            c.setAuditReason(reason);
            c.setUpdatedAt(java.time.LocalDateTime.now());
            log.debug("更新贡献状态: id={}, status={}", id, status);
        }
    }
}
