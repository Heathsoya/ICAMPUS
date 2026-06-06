package com.icampus.app.qa;

import com.icampus.app.qa.dto.request.AskRequest;
import com.icampus.app.qa.dto.request.FeedbackRequest;
import com.icampus.app.qa.dto.response.AskVO;
import com.icampus.app.qa.dto.response.FeedbackVO;
import com.icampus.app.qa.dto.response.HotItemVO;
import com.icampus.app.qa.dto.response.RelatedQuestion;
import com.icampus.core.BizCode;
import com.icampus.core.BizException;
import com.icampus.domain.entity.AnswerFeedback;
import com.icampus.domain.entity.KnowledgeBase;
import com.icampus.domain.entity.QuestionLog;
import com.icampus.domain.enums.AnswerSource;
import com.icampus.domain.repository.AnswerFeedbackRepository;
import com.icampus.domain.repository.KnowledgeBaseRepository;
import com.icampus.domain.repository.QuestionLogRepository;
import com.icampus.domain.spi.LlmAnalysis;
import com.icampus.domain.spi.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能问答核心服务
 * <p>
 * 流程：LLM分析问题 → 检索知识库 → LLM生成最终答案 → 记录日志
 */
public class QnaService {

    private static final Logger log = LoggerFactory.getLogger(QnaService.class);

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final QuestionLogRepository questionLogRepository;
    private final AnswerFeedbackRepository answerFeedbackRepository;
    private final LlmClient llmClient;
    private final com.icampus.app.qa.support.QuestionValidator questionValidator;
    private final com.icampus.app.qa.support.QuestionSegmenter questionSegmenter;

    public QnaService(KnowledgeBaseRepository knowledgeBaseRepository,
                      QuestionLogRepository questionLogRepository,
                      AnswerFeedbackRepository answerFeedbackRepository,
                      LlmClient llmClient,
                      com.icampus.app.qa.support.QuestionValidator questionValidator,
                      com.icampus.app.qa.support.QuestionSegmenter questionSegmenter) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.questionLogRepository = questionLogRepository;
        this.answerFeedbackRepository = answerFeedbackRepository;
        this.llmClient = llmClient;
        this.questionValidator = questionValidator;
        this.questionSegmenter = questionSegmenter;
    }

    /**
     * 核心问答流程
     */
    public AskVO ask(AskRequest request, Long userId) {
        // 先进行问题清洗与校验
        String raw = request.getQuestion();
        String userQuestion = questionValidator.normalize(raw);
        questionValidator.validate(userQuestion);
        log.info("用户提问 [userId={}]: {}", userId, userQuestion);

        // ====== 第1步：分词 + LLM 分析并合并关键词 ======
        List<String> tokens = questionSegmenter.segment(userQuestion);
        LlmAnalysis analysis = llmClient.analyzeQuestion(userQuestion);

        // 合并两部分关键词（保持顺序并去重）
        java.util.LinkedHashSet<String> merged = new java.util.LinkedHashSet<>();
        if (analysis.getKeywords() != null && !analysis.getKeywords().isBlank()) {
            String[] llmKeywords = analysis.getKeywords().trim().split("\\s+");
            for (String k : llmKeywords) {
                if (k != null && !k.isBlank()) merged.add(k);
            }
        }
        if (tokens != null) merged.addAll(tokens);

        String mergedKeywords = String.join(" ", merged);
        log.info("合并关键词用于检索: {} (LLM={}, tokens={})", mergedKeywords, analysis.getKeywords(), tokens);

        // ====== 第2步：检索知识库 ======
        List<KnowledgeBase> kbResults = knowledgeBaseRepository.search(mergedKeywords);
        log.info("知识库检索命中 {} 条", kbResults.size());

        // ====== 第3步：LLM 基于知识库结果生成最终答案 ======
        String finalAnswer;
        AnswerSource answerSource;
        Double confidence;
        String matchedQuestion = null;
        List<RelatedQuestion> relatedQuestions = Collections.emptyList();

        if (!kbResults.isEmpty()) {
            KnowledgeBase bestMatch = kbResults.get(0);
            matchedQuestion = bestMatch.getQuestion();

            if (bestMatch.getAnswer() != null && bestMatch.getAnswer().length() > 20) {
                finalAnswer = llmClient.generateAnswer(userQuestion, bestMatch.getAnswer(), kbResults);
                answerSource = AnswerSource.KNOWLEDGE_BASE;
                confidence = calculateConfidence(userQuestion, bestMatch);
            } else {
                finalAnswer = llmClient.generateAnswer(userQuestion, null, kbResults);
                answerSource = AnswerSource.LLM;
                confidence = 0.5;
            }

                relatedQuestions = kbResults.stream()
                    .skip(1)
                    .limit(5)
                    .map(kb -> new RelatedQuestion(kb.getId(), kb.getQuestion()))
                    .collect(Collectors.toList());
        } else {
            finalAnswer = llmClient.generateAnswer(userQuestion, null, Collections.emptyList());
            answerSource = AnswerSource.NOT_FOUND;
            confidence = 0.3;
        }

        // ====== 第4步：保存问答日志 ======
        QuestionLog logEntry = new QuestionLog();
        logEntry.setUserId(userId);
        logEntry.setQuestion(userQuestion);
        logEntry.setAnswer(finalAnswer);
        logEntry.setMatchedQuestion(matchedQuestion);
        logEntry.setCategory(analysis.getCategory());
        logEntry.setConfidence(confidence);
        logEntry.setAnswerSource(answerSource.getValue());
        // 保存用于日志的 RelatedQuestion 列表（序列化为 JSON）
        logEntry.setRelatedQuestions(toJson(relatedQuestions));
        logEntry.setCreatedAt(LocalDateTime.now());
        QuestionLog saved = questionLogRepository.save(logEntry);

        // ====== 组装响应 ======
        AskVO vo = new AskVO();
        vo.setAnswer(finalAnswer);
        vo.setMatchedQuestion(matchedQuestion);
        vo.setCategory(analysis.getCategory());
        vo.setConfidence(confidence);
        vo.setAnswerSource(answerSource.name());
        // 将 RelatedQuestion DTO 转换为简单字符串列表供前端展示
        List<String> relatedQuestionTexts = relatedQuestions.stream()
            .map(RelatedQuestion::getQuestion)
            .collect(Collectors.toList());
        vo.setRelatedQuestions(relatedQuestionTexts);
        vo.setQuestionLogId(saved.getId());

        return vo;
    }

    /**
     * 提交答案反馈
     */
    public FeedbackVO submitFeedback(FeedbackRequest request, Long userId) {
        QuestionLog questionLog = questionLogRepository.findById(request.getQuestionLogId());
        if (questionLog == null) {
            throw new BizException(BizCode.QUESTION_LOG_NOT_FOUND);
        }

        // 防重复提交
        if (answerFeedbackRepository.existsByQuestionLogId(request.getQuestionLogId())) {
            throw new BizException(BizCode.ALREADY_FEEDBACK);
        }

        AnswerFeedback feedback = new AnswerFeedback();
        feedback.setQuestionLogId(request.getQuestionLogId());
        feedback.setUserId(userId);
        // 将 request 中的 feedbackType 映射到 helpful(boolean)，并使用 feedbackContent 作为 comment
        String feedbackType = request.getFeedbackType();
        Boolean helpful = null;
        if (feedbackType != null) {
            String t = feedbackType.trim().toUpperCase();
            if ("USEFUL".equals(t) || "HELPFUL".equals(t) || "YES".equals(t) || "TRUE".equals(t)) {
                helpful = Boolean.TRUE;
            } else if ("USELESS".equals(t) || "NO".equals(t) || "FALSE".equals(t)) {
                helpful = Boolean.FALSE;
            }
        }
        feedback.setHelpful(helpful);
        feedback.setComment(request.getFeedbackContent());
        feedback.setCreatedAt(LocalDateTime.now());
        answerFeedbackRepository.save(feedback);

        log.info("用户反馈 [questionLogId={}, helpful={}]", request.getQuestionLogId(), helpful);
        FeedbackVO resp = new FeedbackVO();
        resp.setFeedbackId(feedback.getId());
        resp.setQuestionLogId(feedback.getQuestionLogId());
        resp.setRecorded(true);
        resp.setMessage("反馈已记录");
        return resp;
    }

    /**
     * 热点问题 Top-N
     */
    public List<HotItemVO> getHotQuestions(int limit) {
        List<Object[]> results = questionLogRepository.findHotQuestions(limit);
        return results.stream()
                .map(row -> new HotItemVO((String) row[0], (Long) row[1]))
                .collect(Collectors.toList());
    }

    private double calculateConfidence(String userQuestion, KnowledgeBase bestMatch) {
        String kbQuestion = bestMatch.getQuestion();
        if (kbQuestion == null || kbQuestion.isEmpty()) return 0.5;

        int maxLen = Math.max(userQuestion.length(), kbQuestion.length());
        int minLen = Math.min(userQuestion.length(), kbQuestion.length());
        double lengthRatio = (double) minLen / maxLen;

        long commonChars = userQuestion.chars()
                .filter(c -> kbQuestion.indexOf(c) >= 0)
                .count();
        double charOverlap = (double) commonChars / Math.max(userQuestion.length(), 1);

        return Math.min(0.95, (lengthRatio * 0.3 + charOverlap * 0.7));
    }

    private String toJson(List<RelatedQuestion> questions) {
        if (questions == null || questions.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < questions.size(); i++) {
            RelatedQuestion rq = questions.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"questionId\":").append(rq.getQuestionId())
              .append(",\"question\":\"").append(escapeJson(rq.getQuestion())).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
