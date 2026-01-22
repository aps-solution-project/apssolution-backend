package org.example.apssolution.dto.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Tool;
import org.example.apssolution.domain.entity.ToolCategory;
import org.example.apssolution.dto.request.tool.UpsertToolRequest;

import java.util.List;

@Getter
@Setter
@Builder
public class UpsertProductRequest {
    @Valid
    @NotEmpty
    List<UpsertProductRequest.Item> products;

    @Getter
    @Setter
    public static class Item {
        @NotBlank
        private String productId;
        @NotBlank
        private String name;
        private String description;
    }
}
