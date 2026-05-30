package com.icampus.domain.spi;

import com.icampus.domain.entity.KnowledgeBase;

import java.util.List;

/**
 * 大模型客户端 SPI 接口
 * <p>
 * 由 infra 模块提供具体实现（通义千问/文心一言等）。
 * 定义在 domain 层，遵循依赖倒置原则。
 */
public interface LlmClient {

    /**
     * 分析用户问题，提取关键词和分类
     *
     * @param question 用户原始问题
     * @return 分析结果（关键词、分类）
     */
    LlmAnalysis analyzeQuestion(String question);

    /**
     * 基于知识库结果生成最终答案
     *
     * @param userQuestion  用户原始问题
     * @param kbAnswer      最佳匹配的知识库答案（可能为 null）
     * @param kbResults     所有匹配的知识库条目
     * @return 生成的最终答案
     */
    String generateAnswer(String userQuestion, String kbAnswer, List<KnowledgeBase> kbResults);
}
