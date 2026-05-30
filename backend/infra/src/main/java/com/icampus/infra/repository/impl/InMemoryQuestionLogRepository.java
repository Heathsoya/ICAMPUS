package com.icampus.infra.repository.impl;

import com.icampus.domain.entity.QuestionLog;
import com.icampus.domain.repository.QuestionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 问答日志仓储 — 内存实现
 */
public class InMemoryQuestionLogRepository implements QuestionLogRepository {

    private static final Logger log = LoggerFactory.getLogger(InMemoryQuestionLogRepository.class);

    private final Map<Long, QuestionLog> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public QuestionLog save(QuestionLog logEntry) {
        if (logEntry.getId() == null) {
            logEntry.setId(idGenerator.getAndIncrement());
        }
        store.put(logEntry.getId(), logEntry);
        log.debug("保存问答日志: id={}, question={}", logEntry.getId(), logEntry.getQuestion());
        return logEntry;
    }

    @Override
    public QuestionLog findById(Long id) {
        return store.get(id);
    }

    @Override
    public List<Object[]> findHotQuestions(int limit) {
        // 按问题文本分组统计频次
        Map<String, Long> frequencyMap = store.values().stream()
                .collect(Collectors.groupingBy(QuestionLog::getQuestion, Collectors.counting()));

        return frequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .collect(Collectors.toList());
    }
}
