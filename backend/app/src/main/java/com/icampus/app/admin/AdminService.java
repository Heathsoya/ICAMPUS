package com.icampus.app.admin;

import com.icampus.app.admin.audit.dto.request.AuditRequest;
import com.icampus.app.admin.audit.dto.response.AuditItemVO;
import com.icampus.app.admin.db_manage.dto.response.KnowledgeItemVO;
import com.icampus.app.admin.db_manage.dto.response.KnowledgeSummaryVO;
import com.icampus.core.BizCode;
import com.icampus.core.BizException;
import com.icampus.domain.entity.Contribution;
import com.icampus.domain.entity.KnowledgeBase;
import com.icampus.domain.enums.AuditStatusEnum;
import com.icampus.domain.repository.ContributionRepository;
import com.icampus.domain.repository.KnowledgeBaseRepository;
import com.icampus.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * 管理员审核服务
 */
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final ContributionRepository contributionRepository;
    private final UserRepository userRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;

    public AdminService(ContributionRepository contributionRepository,
                        UserRepository userRepository,
                        KnowledgeBaseRepository knowledgeBaseRepository) {
        this.contributionRepository = contributionRepository;
        this.userRepository = userRepository;
        this.knowledgeBaseRepository = knowledgeBaseRepository;
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

    @Transactional
    public void audit(AuditRequest request) {
        Contribution contribution = contributionRepository.findById(request.getId());
        if (contribution == null) {
            throw new BizException(BizCode.CONTRIBUTION_NOT_FOUND);
        }

        try {
            AuditStatusEnum status = AuditStatusEnum.valueOf(request.getStatus().toUpperCase());
            if (status == AuditStatusEnum.APPROVED) {
                KnowledgeBase knowledge = new KnowledgeBase();
                knowledge.setQuestion(contribution.getQuestion());
                knowledge.setAnswer(contribution.getAnswer());
                knowledge.setCategory("综合咨询");
                knowledge.setKeywords(contribution.getQuestion());
                knowledge.setSource("用户贡献");
                knowledge.setCreatedAt(LocalDateTime.now());
                knowledge.setUpdatedAt(LocalDateTime.now());
                knowledgeBaseRepository.save(knowledge);
            }
            contributionRepository.updateStatus(request.getId(), status.getCode(), request.getReason());
        } catch (IllegalArgumentException e) {
            throw new BizException(BizCode.INVALID_AUDIT_STATUS);
        }

        log.info("审核完成 [id={}, status={}]", request.getId(), request.getStatus());
    }

    @Transactional
    public void deleteKnowledge(Long id) {
        if (id == null) {
            throw new BizException(BizCode.BAD_REQUEST);
        }

        KnowledgeBase knowledge = knowledgeBaseRepository.findById(id);
        if (knowledge == null) {
            throw new BizException(BizCode.NOT_FOUND);
        }

        boolean deleted = knowledgeBaseRepository.deleteById(id);
        if (!deleted) {
            throw new BizException(BizCode.NOT_FOUND);
        }

        log.info("知识库条目删除成功 [id={}]", id);
    }

    @Transactional
    public void deleteKnowledgeBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(BizCode.BAD_REQUEST);
        }

        int deletedCount = 0;
        for (Long id : new LinkedHashSet<>(ids)) {
            if (id != null && id > 0 && knowledgeBaseRepository.deleteById(id)) {
                deletedCount++;
            }
        }

        log.info("知识库批量删除完成 [requested={}, deleted={}]", ids.size(), deletedCount);
    }

    public KnowledgeSummaryVO getKnowledgeSummary(int requestedPage, int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, 200));
        List<KnowledgeBase> allItems = knowledgeBaseRepository.findAll();
        int totalPages = Math.max(1, (allItems.size() + limit - 1) / limit);
        int page = Math.min(Math.max(1, requestedPage), totalPages);
        long offset = (long) (page - 1) * limit;

        KnowledgeSummaryVO summary = new KnowledgeSummaryVO();
        summary.setTotal(allItems.size());
        summary.setCrawlerCount(allItems.stream()
                .filter(item -> item.getSource() != null && item.getSource().contains("爬虫"))
                .count());
        summary.setItems(allItems.stream()
                .sorted(Comparator.comparing(KnowledgeBase::getId).reversed())
                .skip(offset)
                .limit(limit)
                .map(this::toKnowledgeItem)
                .collect(Collectors.toList()));
        return summary;
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

    private KnowledgeItemVO toKnowledgeItem(KnowledgeBase knowledge) {
        KnowledgeItemVO item = new KnowledgeItemVO();
        item.setId(knowledge.getId());
        item.setQuestion(knowledge.getQuestion());
        item.setCategory(knowledge.getCategory());
        item.setKeywords(knowledge.getKeywords());
        item.setSource(knowledge.getSource());
        item.setUpdatedAt(knowledge.getUpdatedAt() != null ? knowledge.getUpdatedAt().toString() : null);
        return item;
    }
}
