package org.example.apssolution.dto.response.tool;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.ToolCategory;

import java.util.List;

@Getter
@Setter
@Builder
public class CategoryListResponse {
    List<ToolCategory> categoryList;
}
