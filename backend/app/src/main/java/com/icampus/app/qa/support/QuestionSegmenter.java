package com.icampus.app.qa.support;

import com.icampus.domain.qa.repository.KeywordRepository;
import com.icampus.domain.qa.repository.StopWordRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户问题分词器。
 *
 * 职责：
 * 1. 接收已经由 QuestionValidator.normalize() 清洗后的问题；
 * 2. 从数据库关键词库加载启用关键词；
 * 3. 从数据库停用词库加载启用停用词；
 * 4. 基于关键词库进行最长匹配分词；
 * 5. 使用停用词库过滤无意义词；
 * 6. 返回可用于后续知识库检索的关键词列表。
 *
 * 注意：
 * 本类不负责问题清洗、不负责问题合法性校验。
 * 问题清洗和校验由 QuestionValidator 负责。
 */
@Component
public class QuestionSegmenter {

    private final KeywordRepository keywordRepository;

    private final StopWordRepository stopWordRepository;

    public QuestionSegmenter(
            KeywordRepository keywordRepository,
            StopWordRepository stopWordRepository
    ) {
        this.keywordRepository = keywordRepository;
        this.stopWordRepository = stopWordRepository;
    }

    /**
     * 对已经清洗后的用户问题进行分词。
     *
     * @param normalizedQuestion 已经经过 QuestionValidator.normalize() 处理的问题
     * @return 分词后的关键词列表
     */
    public List<String> segment(String normalizedQuestion) {
        if (normalizedQuestion == null || normalizedQuestion.isBlank()) {
            return List.of();
        }

        List<String> keywordDictionary = loadKeywordDictionary();

        Set<String> stopWordSet = loadStopWordSet();

        List<String> rawTokens = maxMatchSegment(normalizedQuestion, keywordDictionary);

        return filterTokens(rawTokens, stopWordSet);
    }

    /**
     * 从数据库加载关键词库。
     *
     * 按长度降序排列，保证最长匹配优先。
     * 例如优先匹配“座位预约”，而不是先匹配“座位”。
     */
    private List<String> loadKeywordDictionary() {
        List<String> keywords = keywordRepository.findEnabledKeywords();

        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }

        return keywords.stream()
                .filter(word -> word != null && !word.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
    }

    /**
     * 从数据库加载停用词库。
     */
    private Set<String> loadStopWordSet() {
        List<String> stopWords = stopWordRepository.findEnabledStopWords();

        if (stopWords == null || stopWords.isEmpty()) {
            return Set.of();
        }

        return stopWords.stream()
                .filter(word -> word != null && !word.isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    /**
     * 基于关键词库进行最长匹配分词。
     *
     * 示例：
     * 输入：图书馆座位预约怎么弄
     * 关键词库：图书馆、座位预约、预约
     * 输出：图书馆、座位预约
     */
    private List<String> maxMatchSegment(String text, List<String> dictionary) {
        List<String> result = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return result;
        }

        if (dictionary == null || dictionary.isEmpty()) {
            result.add(text);
            return result;
        }

        int index = 0;

        while (index < text.length()) {
            String matchedWord = null;

            for (String keyword : dictionary) {
                if (keyword == null || keyword.isBlank()) {
                    continue;
                }

                if (text.startsWith(keyword, index)) {
                    matchedWord = keyword;
                    break;
                }
            }

            if (matchedWord != null) {
                result.add(matchedWord);
                index += matchedWord.length();
            } else {
                index++;
            }
        }

        return result;
    }

    /**
     * 过滤分词结果：
     * 1. 去掉空词；
     * 2. 去掉单字词；
     * 3. 去掉数据库停用词；
     * 4. 去重。
     */
    private List<String> filterTokens(List<String> tokens, Set<String> stopWordSet) {
        if (tokens == null || tokens.isEmpty()) {
            return List.of();
        }

        return tokens.stream()
                .filter(token -> token != null && !token.isBlank())
                .map(String::trim)
                .filter(token -> token.length() >= 2)
                .filter(token -> !stopWordSet.contains(token))
                .distinct()
                .toList();
    }
}