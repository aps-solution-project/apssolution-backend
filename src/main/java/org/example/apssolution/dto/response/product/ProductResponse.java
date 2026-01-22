package org.example.apssolution.dto.response.product;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Product;

@Getter
@Setter
@Builder
public class ProductResponse {
    Product product;
}
