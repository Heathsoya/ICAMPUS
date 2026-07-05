package com.icampus.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.icampus.infra.persistence.entity.ContributionDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ContributionMapper extends BaseMapper<ContributionDO> {

    @Update("""
            UPDATE contribution
            SET status = #{status},
                audit_reason = #{reason},
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("reason") String reason);
}
