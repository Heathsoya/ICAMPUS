package com.icampus.api.controller;

import com.icampus.api.config.CurrentUserId;
import com.icampus.api.dto.request.ContributionRequest;
import com.icampus.api.dto.response.IdStatusResponse;
import com.icampus.api.service.ContributionService;
import com.icampus.core.ApiResponse;
import com.icampus.core.BusinessException;
import com.icampus.domain.entity.Contribution;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识贡献接口
 * <p>
 * 用户可提交问答知识，经管理员审核后入库。
 */
@RestController
@RequestMapping("/api/contribution")
public class ContributionController {

    private final ContributionService contributionService;

    public ContributionController(ContributionService contributionService) {
        this.contributionService = contributionService;
    }

    /**
     * 提交知识贡献
     * <pre>
     * POST /api/contribution
     * Body: { "question": "...", "answer": "..." }
     * → { "code": 200, "data": { "id": 1, "status": "pending" } }
     * </pre>
     */
    @PostMapping
    public ApiResponse<IdStatusResponse> submit(@Valid @RequestBody ContributionRequest request,
                                                  @CurrentUserId Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录后再提交知识贡献");
        }
        Contribution contribution = contributionService.submit(request, userId);
        return ApiResponse.success(new IdStatusResponse(contribution.getId(), contribution.getStatus()));
    }
}
