package org.example.apssolution.dto.response.product;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Product;

import java.util.List;

@Getter
@Setter
@Builder
public class ProductListResponse {
    List<Product> products;
}
