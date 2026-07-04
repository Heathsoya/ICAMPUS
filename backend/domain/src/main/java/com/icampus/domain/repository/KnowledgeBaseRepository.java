package com.icampus.domain.repository;

import com.icampus.domain.entity.KnowledgeBase;

import java.util.List;

/**
 * 知识库仓储接口（纯接口，由 infra 模块实现）
 */
public interface KnowledgeBaseRepository {

    /**
     * 新增或按标准问题更新知识条目。
     */
    KnowledgeBase save(KnowledgeBase knowledgeBase);

    /**
     * 全文检索匹配的知识库条目
     */
    List<KnowledgeBase> search(String keyword);

    /**
     * 按分类查找
     */
    List<KnowledgeBase> findByCategory(String category);

    /**
     * 获取所有条目（用于热点统计等）
     */
    List<KnowledgeBase> findAll();

    KnowledgeBase findById(Long id);
}
