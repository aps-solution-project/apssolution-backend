package org.example.apssolution.dto.request.tool;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Tool;
import org.example.apssolution.domain.entity.ToolCategory;

import java.util.List;

@Getter
@Setter
@Builder
public class UpsertToolRequest {
    @Valid
    @NotEmpty
    List<Item> tools;

    @Getter
    @Setter
    public static class Item {
        @NotBlank
        private String toolId;
        @NotBlank
        private String categoryId;
        private String description;

        public Tool toTool(ToolCategory category) {
            if (this.description == null) {
                this.description = "";
            }
            return Tool.builder().id(this.toolId).category(category).description(this.description).build();
        }
    }

}
