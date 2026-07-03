package com.icampus.api.controller;

import com.icampus.app.dto.request.AuditRequest;
import com.icampus.app.dto.request.CrawlerScheduleRequest;
import com.icampus.app.dto.response.AuditItemVO;
import com.icampus.app.dto.response.CrawlerStatusVO;
import com.icampus.app.dto.response.KnowledgeSummaryVO;
import com.icampus.app.service.AdminService;
import com.icampus.app.service.CrawlerAdminService;
import com.icampus.core.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员接口
 * <p>
 * 提供贡献审核功能，需 ADMIN 角色。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final CrawlerAdminService crawlerAdminService;

    public AdminController(AdminService adminService,
                           CrawlerAdminService crawlerAdminService) {
        this.adminService = adminService;
        this.crawlerAdminService = crawlerAdminService;
    }

    /**
     * 获取审核列表
     * <pre>
     * GET /api/admin/audit?status=pending
     * → { "code": 200, "data": [{ "id": 1, "question": "...", "status": "pending", ... }] }
     * </pre>
     */
    @GetMapping("/audit")
    public ApiResponse<List<AuditItemVO>> auditList(
            @RequestParam(name = "status", required = false) String status) {
        List<AuditItemVO> list = adminService.getAuditList(status);
        return ApiResponse.success(list);
    }

    /**
     * 获取知识库统计和最近条目。
     */
    @GetMapping("/knowledge")
    public ApiResponse<KnowledgeSummaryVO> knowledgeList(
            @RequestParam(name = "limit", defaultValue = "100") int limit) {
        return ApiResponse.success(adminService.getKnowledgeSummary(limit));
    }

    @GetMapping("/crawler")
    public ApiResponse<CrawlerStatusVO> crawlerStatus() {
        return ApiResponse.success(crawlerAdminService.getStatus());
    }

    @PostMapping("/crawler/run")
    public ApiResponse<CrawlerStatusVO> runCrawler() {
        return ApiResponse.success(crawlerAdminService.trigger());
    }

    @PutMapping("/crawler/schedule")
    public ApiResponse<CrawlerStatusVO> configureCrawler(
            @Valid @RequestBody CrawlerScheduleRequest request) {
        return ApiResponse.success(crawlerAdminService.configure(request));
    }

    /**
     * 执行审核操作
     * <pre>
     * POST /api/admin/audit
     * Body: { "id": 1, "status": "approved", "reason": "内容准确" }
     * → { "code": 200, "message": "success" }
     * </pre>
     */
    @PostMapping("/audit")
    public ApiResponse<Void> audit(@Valid @RequestBody AuditRequest request) {
        adminService.audit(request);
        return ApiResponse.success();
    }
}
