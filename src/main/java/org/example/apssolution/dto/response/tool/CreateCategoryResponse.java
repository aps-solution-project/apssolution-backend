package org.example.apssolution.dto.response.tool;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.ToolCategory;

@Getter
@Setter
@Builder
public class CreateCategoryResponse {
    private ToolCategory toolCategory;
}
