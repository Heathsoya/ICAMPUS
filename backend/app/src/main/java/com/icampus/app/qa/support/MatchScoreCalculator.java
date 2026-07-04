package com.icampus.app.qa.support;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 计算用户问题与知识库知识的匹配度。
 *
 * 匹配依据：
 * 1. 用户问题与标准问题的直接包含关系；
 * 2. 分词关键词是否命中标准问题；
 * 3. 分词关键词是否命中知识库关键词字段；
 * 4. 分词关键词是否命中答案内容；
 * 5. 分词关键词是否命中分类。
 */
@Component
public class MatchScoreCalculator {

    public double calculate(
            String userQuestion,
            List<String> extractedKeywords,
            String standardQuestion,
            String answer,
            String category,
            String storedKeywords
    ) {
        double score = 0.0;

        String q = safe(userQuestion);
        String sq = safe(standardQuestion);
        String ans = safe(answer);
        String cat = safe(category);
        String keys = safe(storedKeywords);

        // 1. 用户问题直接命中标准问题，说明匹配度较高
        if (!q.isBlank() && !sq.isBlank()) {
            if (sq.contains(q) || q.contains(sq)) {
                score += 0.50;
            }
        }

        // 2. 关键词为空时，只返回基础分
        if (extractedKeywords == null || extractedKeywords.isEmpty()) {
            return normalize(score);
        }

        // 3. 去重，避免重复关键词反复加分
        Set<String> uniqueKeywords = new HashSet<>();

        for (String keyword : extractedKeywords) {
            String kw = safe(keyword).trim();

            if (kw.isBlank()) {
                continue;
            }

            uniqueKeywords.add(kw);
        }

        for (String keyword : uniqueKeywords) {

            // 关键词命中标准问题
            if (!sq.isBlank() && sq.contains(keyword)) {
                score += 0.30;
            }

            // 关键词命中知识库关键词字段
            if (containsStoredKeyword(keys, keyword)) {
                score += 0.30;
            }

            // 关键词命中答案正文
            if (!ans.isBlank() && ans.contains(keyword)) {
                score += 0.15;
            }

            // 关键词命中分类
            if (!cat.isBlank() && cat.contains(keyword)) {
                score += 0.10;
            }
        }

        return normalize(score);
    }

    /**
     * 判断关键词是否命中知识库中存储的关键词字段。
     *
     * 支持多种分隔符：
     * ; ； , ， 、 空格
     */
    private boolean containsStoredKeyword(String storedKeywords, String keyword) {
        if (storedKeywords == null || storedKeywords.isBlank()) {
            return false;
        }

        if (keyword == null || keyword.isBlank()) {
            return false;
        }

        String[] parts = storedKeywords.split("[;；,，、\\s]+");

        for (String part : parts) {
            String stored = part.trim();

            if (stored.isBlank()) {
                continue;
            }

            if (stored.equals(keyword) || stored.contains(keyword) || keyword.contains(stored)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 将分数限制在 0~1 之间。
     */
    private double normalize(double score) {
        if (score < 0) {
            return 0.0;
        }

        return Math.min(score, 1.0);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}