package com.icampus.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.icampus.infra.persistence.entity.QaKeywordDO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface QaKeywordMapper extends BaseMapper<QaKeywordDO> {

    @Select("""
            SELECT keyword
            FROM qa_keyword
            WHERE enabled = 1
            ORDER BY weight DESC, CHAR_LENGTH(keyword) DESC, id ASC
            """)
    List<String> selectEnabledKeywords();
}
