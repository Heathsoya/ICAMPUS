package com.icampus.domain.repository;

import com.icampus.domain.entity.QuestionLog;

import java.util.List;

/**
 * 问答日志仓储接口（纯接口，由 infra 模块实现）
 */
public interface QuestionLogRepository {

    QuestionLog save(QuestionLog log);

    QuestionLog findById(Long id);

    /** 热点问题 Top-N */
    List<Object[]> findHotQuestions(int limit);
}
