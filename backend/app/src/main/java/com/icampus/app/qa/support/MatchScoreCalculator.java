package com.icampus.app.qa.support;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 计算用户问题知识库知识的匹配度
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

        if (!q.isBlank() && sq.contains(q)) {
            score += 0.50;
        }

        for (String keyword : extractedKeywords) {
            if (sq.contains(keyword)) {
                score += 0.30;
            }

            if (containsStoredKeyword(keys, keyword)) {
                score += 0.30;
            }

            if (ans.contains(keyword)) {
                score += 0.15;
            }

            if (cat.contains(keyword)) {
                score += 0.10;
            }
        }

        return Math.min(score, 1.0);
    }

    private boolean containsStoredKeyword(String storedKeywords, String keyword) {
        if (storedKeywords == null || storedKeywords.isBlank()) {
            return false;
        }

        String[] parts = storedKeywords.split(";");

        for (String part : parts) {
            if (part.trim().equals(keyword) || part.trim().contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}