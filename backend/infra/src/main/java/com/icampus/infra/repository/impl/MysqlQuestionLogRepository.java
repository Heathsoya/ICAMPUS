package com.icampus.infra.repository.impl;

import com.icampus.domain.entity.QuestionLog;
import com.icampus.domain.repository.QuestionLogRepository;
import com.icampus.infra.persistence.entity.QuestionLogDO;
import com.icampus.infra.persistence.mapper.QuestionLogMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class MysqlQuestionLogRepository implements QuestionLogRepository {

    private final QuestionLogMapper questionLogMapper;

    public MysqlQuestionLogRepository(QuestionLogMapper questionLogMapper) {
        this.questionLogMapper = questionLogMapper;
    }

    @Override
    public QuestionLog save(QuestionLog log) {
        QuestionLogDO data = toData(log);
        if (data.getId() == null) {
            questionLogMapper.insert(data);
        } else {
            questionLogMapper.updateById(data);
        }
        log.setId(data.getId());
        return log;
    }

    @Override
    public QuestionLog findById(Long id) {
        if (id == null) {
            return null;
        }
        return toDomain(questionLogMapper.selectById(id));
    }

    @Override
    public List<Object[]> findHotQuestions(int limit) {
        int safeLimit = limit > 0 ? limit : 10;
        return questionLogMapper.selectHotQuestions(safeLimit).stream()
                .map(this::toHotQuestionRow)
                .toList();
    }

    private Object[] toHotQuestionRow(Map<String, Object> row) {
        Object count = row.get("cnt");
        if (count == null) {
            count = row.get("CNT");
        }
        Long value = count instanceof Number number ? number.longValue() : Long.parseLong(String.valueOf(count));
        Object question = row.get("question");
        if (question == null) {
            question = row.get("QUESTION");
        }
        return new Object[]{String.valueOf(question), value};
    }

    private QuestionLogDO toData(QuestionLog log) {
        if (log == null) {
            return null;
        }
        QuestionLogDO data = new QuestionLogDO();
        data.setId(log.getId());
        data.setUserId(log.getUserId());
        data.setQuestion(log.getQuestion());
        data.setAnswer(log.getAnswer());
        data.setMatchedQuestion(log.getMatchedQuestion());
        data.setCategory(log.getCategory());
        data.setConfidence(log.getConfidence());
        data.setAnswerSource(log.getAnswerSource());
        data.setRelatedQuestions(log.getRelatedQuestions());
        data.setCreatedAt(log.getCreatedAt());
        return data;
    }

    private QuestionLog toDomain(QuestionLogDO data) {
        if (data == null) {
            return null;
        }
        QuestionLog log = new QuestionLog();
        log.setId(data.getId());
        log.setUserId(data.getUserId());
        log.setQuestion(data.getQuestion());
        log.setAnswer(data.getAnswer());
        log.setMatchedQuestion(data.getMatchedQuestion());
        log.setCategory(data.getCategory());
        log.setConfidence(data.getConfidence());
        log.setAnswerSource(data.getAnswerSource());
        log.setRelatedQuestions(data.getRelatedQuestions());
        log.setCreatedAt(data.getCreatedAt());
        return log;
    }
}
