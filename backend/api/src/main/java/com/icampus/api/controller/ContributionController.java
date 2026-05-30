package com.icampus.api.controller;

import com.icampus.api.config.CurrentUserId;
import com.icampus.app.dto.request.ContributionRequest;
import com.icampus.app.dto.response.ContributionVO;
import com.icampus.app.service.ContributionService;
import com.icampus.core.ApiResponse;
import com.icampus.core.BizCode;
import com.icampus.core.BizException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识贡献接口
 */
@RestController
@RequestMapping("/api/contribution")
public class ContributionController {

    private final ContributionService contributionService;

    public ContributionController(ContributionService contributionService) {
        this.contributionService = contributionService;
    }

    @PostMapping
    public ApiResponse<ContributionVO> submit(@Valid @RequestBody ContributionRequest request,
                                               @CurrentUserId Long userId) {
        if (userId == null) {
            throw new BizException(BizCode.UNAUTHORIZED);
        }
        return ApiResponse.success(contributionService.submit(request, userId));
    }
}
