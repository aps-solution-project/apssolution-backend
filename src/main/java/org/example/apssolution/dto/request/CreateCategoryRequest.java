package org.example.apssolution.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.ToolCategory;

@Getter
@Setter
public class CreateCategoryRequest {
    @NotBlank
    private String categoryId;
    @NotBlank
    private String name;

    public ToolCategory toToolCategory() {
        return ToolCategory.builder().id(this.categoryId).name(this.name).build();
    }
}
