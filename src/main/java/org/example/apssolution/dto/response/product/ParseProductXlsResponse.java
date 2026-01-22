package org.example.apssolution.dto.response.product;

import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Product;

import java.util.List;

@Getter
@Setter
public class ParseProductXlsResponse {
    List<Product> products;
}
