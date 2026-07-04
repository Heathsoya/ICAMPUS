package com.icampus.app.contribution;

import com.icampus.app.contribution.dto.request.ContributionRequest;
import com.icampus.app.contribution.dto.response.ContributionVO;
import com.icampus.core.BizCode;
import com.icampus.core.BizException;
import com.icampus.domain.entity.Contribution;
import com.icampus.domain.enums.AuditStatusEnum;
import com.icampus.domain.repository.ContributionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * 用户知识贡献服务
 */
public class ContributionService {

    private static final Logger log = LoggerFactory.getLogger(ContributionService.class);

    private final ContributionRepository contributionRepository;

    public ContributionService(ContributionRepository contributionRepository) {
        this.contributionRepository = contributionRepository;
    }

    public ContributionVO submit(ContributionRequest request, Long userId) {
        // 敏感词检测
        if (containsSensitiveWord(request.getQuestion()) || containsSensitiveWord(request.getAnswer())) {
            throw new BizException(BizCode.SENSITIVE_WORD);
        }

        Contribution contribution = new Contribution();
        contribution.setUserId(userId);
        contribution.setQuestion(request.getQuestion().trim());
        contribution.setAnswer(request.getAnswer().trim());
        contribution.setStatus(AuditStatusEnum.PENDING.getCode());
        contribution.setCreatedAt(LocalDateTime.now());
        contribution.setUpdatedAt(LocalDateTime.now());

        Contribution saved = contributionRepository.save(contribution);
        log.info("用户提交知识贡献 [userId={}, id={}]", userId, saved.getId());
        return new ContributionVO(saved.getId(), saved.getStatus());
    }

    private boolean containsSensitiveWord(String text) {
        // TODO: 对接敏感词库
        return false;
    }
}
