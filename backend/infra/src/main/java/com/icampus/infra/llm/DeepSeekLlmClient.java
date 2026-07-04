package com.icampus.infra.llm;

import com.icampus.domain.entity.KnowledgeBase;
import com.icampus.domain.spi.LlmAnalysis;
import com.icampus.domain.spi.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DeepSeek RAG 客户端
 * <p>
 * RAG 流程：知识库检索 → 拼入 Prompt 作为上下文 → DeepSeek 生成答案 → 降级兜底
 */
public class DeepSeekLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekLlmClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;
    private final String apiUrl;
    private final String model;

    public DeepSeekLlmClient(String apiKey, String model, String apiUrl) {
        this.model = model != null ? model : "deepseek-chat";
        this.apiUrl = apiUrl != null ? apiUrl : "https://api.deepseek.com/v1/chat/completions";
        this.webClient = WebClient.builder()
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public LlmAnalysis analyzeQuestion(String question) {
        String keywords = question.replaceAll("[\\p{P}\\p{S}\\s]+", " ").trim();
        String category = guessCategory(question);
        return new LlmAnalysis(keywords, category);
    }

    @Override
    public String generateAnswer(String userQuestion, String kbAnswer, List<KnowledgeBase> kbResults) {
        String context = buildRagContext(kbResults);
        String systemPrompt = buildSystemPrompt(context);

        try {
            String answer = callDeepSeek(systemPrompt, userQuestion);
            if (answer != null && !answer.isBlank()) {
                return answer;
            }
        } catch (Exception e) {
            log.warn("DeepSeek 调用失败，降级: {}", e.getMessage());
        }

        return fallbackAnswer(userQuestion, kbAnswer, kbResults);
    }

    // ========== RAG ==========

    private String buildRagContext(List<KnowledgeBase> kbResults) {
        if (kbResults == null || kbResults.isEmpty()) {
            return "暂无相关知识库内容，请根据你的知识回答。";
        }
        return kbResults.stream()
                .limit(5)
                .map(kb -> "【问题】" + kb.getQuestion() + "\n【答案】" + kb.getAnswer())
                .collect(Collectors.joining("\n\n"));
    }

    private String buildSystemPrompt(String context) {
        return """
                你是 ICAMPUS 校园智能助手，用中文回答学生问题。
                严格基于以下知识库内容作答。知识库不满足时如实说明。
                要求：简洁清晰、分点列出、语气亲切。

                === 知识库 ===
                %s
                """.formatted(context);
    }

    @SuppressWarnings("unchecked")
    private String callDeepSeek(String systemPrompt, String userPrompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.3,
                "max_tokens", 800
        );

        Map<String, Object> resp = webClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(TIMEOUT)
                .block();

        if (resp != null && resp.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
                return (String) msg.get("content");
            }
        }
        return null;
    }

    private String fallbackAnswer(String userQuestion, String kbAnswer, List<KnowledgeBase> kbResults) {
        if (kbAnswer != null && !kbAnswer.isEmpty()) return kbAnswer;
        if (kbResults != null && !kbResults.isEmpty()) return kbResults.get(0).getAnswer();
        return "抱歉，暂时无法回答「" + userQuestion + "」。\n建议：用更简洁的关键词重新提问，或查看校园官网。";
    }

    private String guessCategory(String question) {
        if (question.contains("宿舍") || question.contains("住宿")) return "住宿生活";
        if (question.contains("食堂") || question.contains("餐厅")) return "餐饮服务";
        if (question.contains("选课") || question.contains("考试") || question.contains("学分")) return "教务教学";
        if (question.contains("图书馆") || question.contains("借书")) return "图书馆";
        if (question.contains("学费") || question.contains("奖学")) return "财务缴费";
        if (question.contains("社团") || question.contains("活动")) return "校园活动";
        if (question.contains("毕业") || question.contains("就业")) return "就业毕业";
        if (question.contains("校园卡") || question.contains("网络")) return "校园服务";
        return "综合咨询";
    }
}
