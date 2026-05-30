package com.icampus.domain.repository;

import com.icampus.domain.entity.AnswerFeedback;

/**
 * 答案反馈仓储接口（纯接口，由 infra 模块实现）
 */
public interface AnswerFeedbackRepository {

    AnswerFeedback save(AnswerFeedback feedback);

    boolean existsByQuestionLogId(Long questionLogId);
}
