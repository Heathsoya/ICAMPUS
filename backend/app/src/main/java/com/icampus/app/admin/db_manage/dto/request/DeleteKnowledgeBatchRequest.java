package com.icampus.app.admin.db_manage.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Batch knowledge-base deletion request.
 */
public class DeleteKnowledgeBatchRequest {

    @NotEmpty(message = "请选择要删除的知识库记录")
    private List<@NotNull @Positive Long> ids;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
