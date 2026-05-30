package com.icampus.api.service;

import com.icampus.api.dto.request.AskRequest;
import com.icampus.api.dto.request.FeedbackRequest;
import com.icampus.api.dto.response.AskResponse;
import com.icampus.api.dto.response.HotQuestionResponse;
import com.icampus.api.dto.response.RelatedQuestion;
import com.icampus.core.BusinessException;
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

    public QnaService(KnowledgeBaseRepository knowledgeBaseRepository,
                      QuestionLogRepository questionLogRepository,
                      AnswerFeedbackRepository answerFeedbackRepository,
                      LlmClient llmClient) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.questionLogRepository = questionLogRepository;
        this.answerFeedbackRepository = answerFeedbackRepository;
        this.llmClient = llmClient;
    }

    /**
     * 核心问答流程
     */
    public AskResponse ask(AskRequest request, Long userId) {
        String userQuestion = request.getQuestion().trim();
        log.info("用户提问 [userId={}]: {}", userId, userQuestion);

        // ====== 第1步：LLM 分析问题，提取关键词和意图 ======
        LlmAnalysis analysis = llmClient.analyzeQuestion(userQuestion);
        log.info("LLM分析结果: keywords={}, category={}", analysis.getKeywords(), analysis.getCategory());

        // ====== 第2步：检索知识库 ======
        List<KnowledgeBase> kbResults = knowledgeBaseRepository.search(analysis.getKeywords());
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
        logEntry.setRelatedQuestions(toJson(relatedQuestions));
        logEntry.setCreatedAt(LocalDateTime.now());
        QuestionLog saved = questionLogRepository.save(logEntry);

        // ====== 组装响应 ======
        AskResponse response = new AskResponse();
        response.setAnswer(finalAnswer);
        response.setMatchedQuestion(matchedQuestion);
        response.setCategory(analysis.getCategory());
        response.setConfidence(confidence);
        response.setAnswerSource(answerSource.name());
        response.setRelatedQuestions(relatedQuestions);
        response.setQuestionLogId(saved.getId());

        return response;
    }

    /**
     * 提交答案反馈
     */
    public void submitFeedback(FeedbackRequest request, Long userId) {
        QuestionLog questionLog = questionLogRepository.findById(request.getQuestionLogId());
        if (questionLog == null) {
            throw new BusinessException(404, "问答日志不存在");
        }

        AnswerFeedback feedback = new AnswerFeedback();
        feedback.setQuestionLogId(request.getQuestionLogId());
        feedback.setUserId(userId);
        feedback.setHelpful(request.getHelpful());
        feedback.setComment(request.getComment());
        feedback.setCreatedAt(LocalDateTime.now());
        answerFeedbackRepository.save(feedback);

        log.info("用户反馈 [questionLogId={}, helpful={}]", request.getQuestionLogId(), request.getHelpful());
    }

    /**
     * 热点问题 Top-N
     */
    public List<HotQuestionResponse> getHotQuestions(int limit) {
        List<Object[]> results = questionLogRepository.findHotQuestions(limit);
        return results.stream()
                .map(row -> new HotQuestionResponse((String) row[0], (Long) row[1]))
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
