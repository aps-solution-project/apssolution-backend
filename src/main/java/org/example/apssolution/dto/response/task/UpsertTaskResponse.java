package org.example.apssolution.dto.response.task;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpsertTaskResponse {
    private int created;
    private int updated;
    private int deleted;
}
