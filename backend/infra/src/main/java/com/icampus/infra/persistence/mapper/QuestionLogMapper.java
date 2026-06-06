package com.icampus.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.icampus.infra.persistence.entity.QuestionLogDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface QuestionLogMapper extends BaseMapper<QuestionLogDO> {

    @Select("""
            SELECT question, COUNT(*) AS cnt
            FROM question_log
            GROUP BY question
            ORDER BY cnt DESC, question ASC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectHotQuestions(@Param("limit") int limit);
}
