package org.example.apssolution.dto.response.tool;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Tool;

@Getter
@Setter
@Builder
public class UpsertToolResponse {
    private int created;
    private int updated;
    private int deleted;
}
