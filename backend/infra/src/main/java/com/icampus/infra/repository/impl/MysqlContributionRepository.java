package com.icampus.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.icampus.domain.entity.Contribution;
import com.icampus.domain.repository.ContributionRepository;
import com.icampus.infra.persistence.entity.ContributionDO;
import com.icampus.infra.persistence.mapper.ContributionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MysqlContributionRepository implements ContributionRepository {

    private final ContributionMapper contributionMapper;

    public MysqlContributionRepository(ContributionMapper contributionMapper) {
        this.contributionMapper = contributionMapper;
    }

    @Override
    public Contribution save(Contribution contribution) {
        ContributionDO data = toData(contribution);
        if (data.getId() == null) {
            contributionMapper.insert(data);
        } else {
            contributionMapper.updateById(data);
        }
        contribution.setId(data.getId());
        return contribution;
    }

    @Override
    public Contribution findById(Long id) {
        if (id == null) {
            return null;
        }
        return toDomain(contributionMapper.selectById(id));
    }

    @Override
    public List<Contribution> findByStatus(String status) {
        LambdaQueryWrapper<ContributionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null && !status.isBlank(), ContributionDO::getStatus, status)
                .orderByDesc(ContributionDO::getCreatedAt)
                .orderByAsc(ContributionDO::getId);
        return contributionMapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Contribution> findAll() {
        LambdaQueryWrapper<ContributionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ContributionDO::getCreatedAt).orderByAsc(ContributionDO::getId);
        return contributionMapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void updateStatus(Long id, String status, String reason) {
        if (id != null) {
            contributionMapper.updateStatus(id, status, reason);
        }
    }

    private ContributionDO toData(Contribution contribution) {
        if (contribution == null) {
            return null;
        }
        ContributionDO data = new ContributionDO();
        data.setId(contribution.getId());
        data.setUserId(contribution.getUserId());
        data.setQuestion(contribution.getQuestion());
        data.setAnswer(contribution.getAnswer());
        data.setStatus(contribution.getStatus());
        data.setAuditReason(contribution.getAuditReason());
        data.setCreatedAt(contribution.getCreatedAt());
        data.setUpdatedAt(contribution.getUpdatedAt());
        return data;
    }

    private Contribution toDomain(ContributionDO data) {
        if (data == null) {
            return null;
        }
        Contribution contribution = new Contribution();
        contribution.setId(data.getId());
        contribution.setUserId(data.getUserId());
        contribution.setQuestion(data.getQuestion());
        contribution.setAnswer(data.getAnswer());
        contribution.setStatus(data.getStatus());
        contribution.setAuditReason(data.getAuditReason());
        contribution.setCreatedAt(data.getCreatedAt());
        contribution.setUpdatedAt(data.getUpdatedAt());
        return contribution;
    }
}
