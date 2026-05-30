package com.icampus.infra.repository.impl;

import com.icampus.domain.entity.AnswerFeedback;
import com.icampus.domain.repository.AnswerFeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 答案反馈仓储 — 内存实现
 */
public class InMemoryAnswerFeedbackRepository implements AnswerFeedbackRepository {

    private static final Logger log = LoggerFactory.getLogger(InMemoryAnswerFeedbackRepository.class);

    private final Map<Long, AnswerFeedback> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public AnswerFeedback save(AnswerFeedback feedback) {
        if (feedback.getId() == null) {
            feedback.setId(idGenerator.getAndIncrement());
        }
        store.put(feedback.getId(), feedback);
        log.debug("保存反馈: id={}, questionLogId={}, helpful={}",
                feedback.getId(), feedback.getQuestionLogId(), feedback.getHelpful());
        return feedback;
    }

    @Override
    public boolean existsByQuestionLogId(Long questionLogId) {
        return store.values().stream()
                .anyMatch(f -> questionLogId.equals(f.getQuestionLogId()));
    }
}
