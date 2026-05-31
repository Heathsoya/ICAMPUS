package com.icampus.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.icampus.domain.entity.KnowledgeBase;
import com.icampus.domain.repository.KnowledgeBaseRepository;
import com.icampus.infra.persistence.entity.KnowledgeBaseDO;
import com.icampus.infra.persistence.mapper.KnowledgeBaseMapper;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class MysqlKnowledgeBaseRepository implements KnowledgeBaseRepository {

    private static final int SEARCH_LIMIT = 50;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public MysqlKnowledgeBaseRepository(KnowledgeBaseMapper knowledgeBaseMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    @Override
    public List<KnowledgeBase> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return toDomainList(knowledgeBaseMapper.selectTop(SEARCH_LIMIT));
        }

        List<KnowledgeBaseDO> fulltextResults = knowledgeBaseMapper.selectByFulltext(keyword.trim(), SEARCH_LIMIT);
        if (fulltextResults != null && !fulltextResults.isEmpty()) {
            return toDomainList(fulltextResults);
        }

        List<String> keywords = Arrays.stream(keyword.trim().split("\\s+"))
                .filter(item -> item != null && !item.isBlank())
                .distinct()
                .toList();
        if (keywords.isEmpty()) {
            return toDomainList(knowledgeBaseMapper.selectTop(SEARCH_LIMIT));
        }
        return toDomainList(knowledgeBaseMapper.selectByKeywordsLike(keywords, SEARCH_LIMIT));
    }

    @Override
    public List<KnowledgeBase> findByCategory(String category) {
        LambdaQueryWrapper<KnowledgeBaseDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(category != null && !category.isBlank(), KnowledgeBaseDO::getCategory, category)
                .orderByAsc(KnowledgeBaseDO::getId);
        return toDomainList(knowledgeBaseMapper.selectList(wrapper));
    }

    @Override
    public List<KnowledgeBase> findAll() {
        LambdaQueryWrapper<KnowledgeBaseDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(KnowledgeBaseDO::getId);
        return toDomainList(knowledgeBaseMapper.selectList(wrapper));
    }

    @Override
    public KnowledgeBase findById(Long id) {
        if (id == null) {
            return null;
        }
        return toDomain(knowledgeBaseMapper.selectById(id));
    }

    private List<KnowledgeBase> toDomainList(List<KnowledgeBaseDO> dataList) {
        return dataList.stream().map(this::toDomain).toList();
    }

    private KnowledgeBase toDomain(KnowledgeBaseDO data) {
        if (data == null) {
            return null;
        }
        KnowledgeBase item = new KnowledgeBase();
        item.setId(data.getId());
        item.setQuestion(data.getQuestion());
        item.setAnswer(data.getAnswer());
        item.setCategory(data.getCategory());
        item.setKeywords(data.getKeywords());
        item.setSource(data.getSource());
        item.setCreatedAt(data.getCreatedAt());
        item.setUpdatedAt(data.getUpdatedAt());
        return item;
    }
}
