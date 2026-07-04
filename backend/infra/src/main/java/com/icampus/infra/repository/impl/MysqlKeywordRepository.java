package com.icampus.infra.repository.impl;

import com.icampus.domain.qa.repository.KeywordRepository;
import com.icampus.infra.persistence.mapper.QaKeywordMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MysqlKeywordRepository implements KeywordRepository {

    private final QaKeywordMapper qaKeywordMapper;

    public MysqlKeywordRepository(QaKeywordMapper qaKeywordMapper) {
        this.qaKeywordMapper = qaKeywordMapper;
    }

    @Override
    public List<String> findEnabledKeywords() {
        return qaKeywordMapper.selectEnabledKeywords();
    }
}
