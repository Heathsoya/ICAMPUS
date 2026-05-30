package com.icampus.infra.llm;

import com.icampus.domain.entity.KnowledgeBase;
import com.icampus.domain.spi.LlmAnalysis;
import com.icampus.domain.spi.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 大模型客户端 Mock 实现
 * <p>
 * 开发阶段使用，后续替换为真实的通义千问/文心一言 API 调用。
 * Mock 策略：基于关键词做简单匹配，无法匹配时返回兜底回答。
 */
public class MockLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(MockLlmClient.class);

    @Override
    public LlmAnalysis analyzeQuestion(String question) {
        // 简单关键词提取：去除标点符号和空白，保留中文和英文
        String keywords = question.replaceAll("[\\p{P}\\p{S}\\s]+", " ").trim();

        // 简单分类推断
        String category = guessCategory(question);

        return new LlmAnalysis(keywords, category);
    }

    @Override
    public String generateAnswer(String userQuestion, String kbAnswer, List<KnowledgeBase> kbResults) {
        // 如果有知识库答案，直接返回（带来源标注）
        if (kbAnswer != null && !kbAnswer.isEmpty()) {
            return kbAnswer;
        }

        // 如果知识库有其他匹配项，尝试拼接
        if (!kbResults.isEmpty()) {
            KnowledgeBase best = kbResults.get(0);
            return best.getAnswer() != null ? best.getAnswer()
                    : "关于「" + best.getQuestion() + "」，我们正在整理相关信息，请稍后再来查询。";
        }

        // 兜底回答
        return "抱歉，暂时无法回答「" + userQuestion + "」这个问题。\n"
                + "建议您：\n"
                + "1. 尝试用更简洁的关键词重新提问\n"
                + "2. 查看校园官网获取最新信息\n"
                + "3. 在「知识贡献」中提交您了解的信息，帮助我们完善知识库";
    }

    /**
     * 根据关键词猜测问题分类
     */
    private String guessCategory(String question) {
        if (question.contains("宿舍") || question.contains("住宿") || question.contains("寝室")) {
            return "住宿生活";
        }
        if (question.contains("食堂") || question.contains("餐厅") || question.contains("饭")) {
            return "餐饮服务";
        }
        if (question.contains("课程") || question.contains("选课") || question.contains("学分") || question.contains("考试")) {
            return "教务教学";
        }
        if (question.contains("图书馆") || question.contains("借书") || question.contains("自习")) {
            return "图书馆";
        }
        if (question.contains("学费") || question.contains("缴费") || question.contains("奖学") || question.contains("助学金")) {
            return "财务缴费";
        }
        if (question.contains("社团") || question.contains("活动") || question.contains("比赛")) {
            return "校园活动";
        }
        if (question.contains("就业") || question.contains("实习") || question.contains("毕业")) {
            return "就业毕业";
        }
        if (question.contains("校园卡") || question.contains("一卡通") || question.contains("网络")) {
            return "校园服务";
        }
        return "综合咨询";
    }
}
