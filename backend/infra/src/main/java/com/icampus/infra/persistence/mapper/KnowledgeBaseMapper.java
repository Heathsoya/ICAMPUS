package com.icampus.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.icampus.infra.persistence.entity.KnowledgeBaseDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBaseDO> {

    @Insert("""
            INSERT INTO knowledge_base
                (question, answer, category, keywords, source, created_at, updated_at)
            VALUES
                (#{question}, #{answer}, #{category}, #{keywords}, #{source}, #{createdAt}, #{updatedAt})
            ON DUPLICATE KEY UPDATE
                id = LAST_INSERT_ID(id),
                answer = VALUES(answer),
                category = VALUES(category),
                keywords = VALUES(keywords),
                source = VALUES(source),
                updated_at = VALUES(updated_at)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int upsert(KnowledgeBaseDO data);

    @Select("""
            SELECT *
            FROM knowledge_base
            ORDER BY id ASC
            LIMIT #{limit}
            """)
    List<KnowledgeBaseDO> selectTop(@Param("limit") int limit);

    @Select("""
            SELECT *,
                   MATCH(question, answer) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) AS score
            FROM knowledge_base
            WHERE MATCH(question, answer) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE)
            ORDER BY score DESC, id ASC
            LIMIT #{limit}
            """)
    List<KnowledgeBaseDO> selectByFulltext(@Param("keyword") String keyword, @Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT *",
            "FROM knowledge_base",
            "<where>",
            "  <foreach collection='keywords' item='keyword' separator=' OR '>",
            "    (question LIKE CONCAT('%', #{keyword}, '%')",
            "     OR answer LIKE CONCAT('%', #{keyword}, '%')",
            "     OR keywords LIKE CONCAT('%', #{keyword}, '%'))",
            "  </foreach>",
            "</where>",
            "ORDER BY id ASC",
            "LIMIT #{limit}",
            "</script>"
    })
    List<KnowledgeBaseDO> selectByKeywordsLike(@Param("keywords") List<String> keywords, @Param("limit") int limit);
}
