package org.example.apssolution.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Tool;
import org.example.apssolution.domain.entity.ToolCategory;

@Getter
@Setter
public class CreateToolRequest {
    @NotBlank
    private String toolId;
    @NotBlank
    private String categoryId;
    private String description;

    public Tool toTool(ToolCategory category) {
        if(this.description == null){
            this.description = "";
        }
        return Tool.builder().id(this.toolId).category(category).description(this.description).build();
    }
}
