package org.example.apssolution.controller;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Product;
import org.example.apssolution.repository.ProductRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/products")
public class ProductController {
    final ProductRepository productRepository;


}
