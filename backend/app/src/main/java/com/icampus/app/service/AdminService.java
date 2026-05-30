package com.icampus.app.service;

import com.icampus.app.dto.request.AuditRequest;
import com.icampus.app.dto.response.AuditItemVO;
import com.icampus.core.BizCode;
import com.icampus.core.BizException;
import com.icampus.domain.entity.Contribution;
import com.icampus.domain.enums.AuditStatusEnum;
import com.icampus.domain.repository.ContributionRepository;
import com.icampus.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员审核服务
 */
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final ContributionRepository contributionRepository;
    private final UserRepository userRepository;

    public AdminService(ContributionRepository contributionRepository,
                        UserRepository userRepository) {
        this.contributionRepository = contributionRepository;
        this.userRepository = userRepository;
    }

    public List<AuditItemVO> getAuditList(String statusFilter) {
        List<Contribution> contributions;
        if (statusFilter != null && !statusFilter.isEmpty()) {
            contributions = contributionRepository.findByStatus(statusFilter);
        } else {
            contributions = contributionRepository.findAll();
        }

        return contributions.stream()
                .map(this::toAuditItem)
                .collect(Collectors.toList());
    }

    public void audit(AuditRequest request) {
        Contribution contribution = contributionRepository.findById(request.getId());
        if (contribution == null) {
            throw new BizException(BizCode.CONTRIBUTION_NOT_FOUND);
        }

        try {
            AuditStatusEnum status = AuditStatusEnum.valueOf(request.getStatus().toUpperCase());
            contributionRepository.updateStatus(request.getId(), status.getCode(), request.getReason());
        } catch (IllegalArgumentException e) {
            throw new BizException(BizCode.INVALID_AUDIT_STATUS);
        }

        log.info("审核完成 [id={}, status={}]", request.getId(), request.getStatus());
    }

    private AuditItemVO toAuditItem(Contribution c) {
        AuditItemVO item = new AuditItemVO();
        item.setId(c.getId());
        item.setQuestion(c.getQuestion());
        item.setAnswer(c.getAnswer());
        item.setStatus(c.getStatus());
        item.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);

        if (c.getUserId() != null) {
            userRepository.findById(c.getUserId())
                    .ifPresent(user -> item.setSubmitter(user.getUsername()));
        }
        return item;
    }
}
