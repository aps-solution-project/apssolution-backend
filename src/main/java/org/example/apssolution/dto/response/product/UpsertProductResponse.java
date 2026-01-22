package org.example.apssolution.dto.response.product;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpsertProductResponse {
    private int created;
    private int updated;
    private int deleted;
}
