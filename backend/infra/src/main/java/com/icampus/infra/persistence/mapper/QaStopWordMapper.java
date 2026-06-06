package com.icampus.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.icampus.infra.persistence.entity.QaStopWordDO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface QaStopWordMapper extends BaseMapper<QaStopWordDO> {

    @Select("""
            SELECT word
            FROM qa_stop_word
            WHERE enabled = 1
            ORDER BY id ASC
            """)
    List<String> selectEnabledStopWords();
}
