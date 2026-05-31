package com.icampus.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.icampus.domain.entity.AnswerFeedback;
import com.icampus.domain.repository.AnswerFeedbackRepository;
import com.icampus.infra.persistence.entity.AnswerFeedbackDO;
import com.icampus.infra.persistence.mapper.AnswerFeedbackMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MysqlAnswerFeedbackRepository implements AnswerFeedbackRepository {

    private final AnswerFeedbackMapper answerFeedbackMapper;

    public MysqlAnswerFeedbackRepository(AnswerFeedbackMapper answerFeedbackMapper) {
        this.answerFeedbackMapper = answerFeedbackMapper;
    }

    @Override
    public AnswerFeedback save(AnswerFeedback feedback) {
        AnswerFeedbackDO data = toData(feedback);
        if (data.getId() == null) {
            answerFeedbackMapper.insert(data);
        } else {
            answerFeedbackMapper.updateById(data);
        }
        feedback.setId(data.getId());
        return feedback;
    }

    @Override
    public boolean existsByQuestionLogId(Long questionLogId) {
        if (questionLogId == null) {
            return false;
        }
        LambdaQueryWrapper<AnswerFeedbackDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerFeedbackDO::getQuestionLogId, questionLogId);
        return answerFeedbackMapper.selectCount(wrapper) > 0;
    }

    private AnswerFeedbackDO toData(AnswerFeedback feedback) {
        if (feedback == null) {
            return null;
        }
        AnswerFeedbackDO data = new AnswerFeedbackDO();
        data.setId(feedback.getId());
        data.setQuestionLogId(feedback.getQuestionLogId());
        data.setUserId(feedback.getUserId());
        data.setHelpful(feedback.getHelpful());
        data.setComment(feedback.getComment());
        data.setCreatedAt(feedback.getCreatedAt());
        return data;
    }
}
