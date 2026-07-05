package com.icampus.infra.repository.impl;

import com.icampus.domain.qa.repository.StopWordRepository;
import com.icampus.infra.persistence.mapper.QaStopWordMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MysqlStopWordRepository implements StopWordRepository {

    private final QaStopWordMapper qaStopWordMapper;

    public MysqlStopWordRepository(QaStopWordMapper qaStopWordMapper) {
        this.qaStopWordMapper = qaStopWordMapper;
    }

    @Override
    public List<String> findEnabledStopWords() {
        return qaStopWordMapper.selectEnabledStopWords();
    }
}
