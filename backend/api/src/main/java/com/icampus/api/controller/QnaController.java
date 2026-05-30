package com.icampus.api.controller;

import com.icampus.api.config.CurrentUserId;
import com.icampus.app.dto.request.AskRequest;
import com.icampus.app.dto.request.FeedbackRequest;
import com.icampus.app.dto.response.AskVO;
import com.icampus.app.dto.response.FeedbackVO;
import com.icampus.app.dto.response.HotItemVO;
import com.icampus.app.service.QnaService;
import com.icampus.core.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 智能问答接口
 * <p>
 * 核心 API：提问、反馈、热点榜。
 * 问答流程：LLM分析问题 → 检索知识库 → LLM生成答案 → 记录日志。
 */
@RestController
@RequestMapping("/api/qna")
public class QnaController {

    private final QnaService qnaService;

    public QnaController(QnaService qnaService) {
        this.qnaService = qnaService;
    }

    /**
     * 智能问答
     * <pre>
     * POST /api/qna/ask
     * Body: { "question": "宿舍几点关门？" }
     * →
     * {
     *   "code": 200,
     *   "data": {
     *     "answer": "学生宿舍每晚23:00关门...",
     *     "matchedQuestion": "宿舍几点关门？",
     *     "category": "住宿生活",
     *     "confidence": 0.85,
     *     "answerSource": "KNOWLEDGE_BASE",
     *     "relatedQuestions": [{ "questionId": 2, "question": "..." }],
     *     "questionLogId": 1
     *   }
     * }
     * </pre>
     */
    @PostMapping("/ask")
    public ApiResponse<AskVO> ask(@Valid @RequestBody AskRequest request,
                                   @CurrentUserId Long userId) {
        return ApiResponse.success(qnaService.ask(request, userId));
    }

    /**
     * 答案反馈
     * <pre>
     * POST /api/qna/feedback
     * Body: { "questionLogId": 1, "helpful": true, "comment": "很有帮助" }
     * → { "code": 200, "message": "success" }
     * </pre>
     */
    @PostMapping("/feedback")
    public ApiResponse<FeedbackVO> feedback(@Valid @RequestBody FeedbackRequest request,
                                             @CurrentUserId Long userId) {
        return ApiResponse.success(qnaService.submitFeedback(request, userId));
    }

    /**
     * 热点问题榜单
     * <pre>
     * GET /api/qna/hot?limit=10
     * → { "code": 200, "data": [{ "question": "宿舍几点关门？", "count": 156 }, ...] }
     * </pre>
     */
    @GetMapping("/hot")
    public ApiResponse<List<HotItemVO>> hotQuestions(
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ApiResponse.success(qnaService.getHotQuestions(Math.min(limit, 50)));
    }
}
