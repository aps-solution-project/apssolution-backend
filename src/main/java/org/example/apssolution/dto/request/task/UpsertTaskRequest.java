package org.example.apssolution.dto.request.task;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.dto.request.tool.UpsertToolRequest;

import java.util.List;

@Getter
@Setter
@Builder
public class UpsertTaskRequest {
    @Valid
    @NotEmpty
    List<UpsertTaskRequest.Item> tasks;

    @Getter
    @Setter
    public static class Item {
        @NotBlank
        private String taskId;
        @NotBlank
        private String productId;
        @NotBlank
        private String categoryId;
        @Positive
        private int seq;
        @NotBlank
        private String name;
        @Positive
        private int duration;

        private String description;
    }
}
