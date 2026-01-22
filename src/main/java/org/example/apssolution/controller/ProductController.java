package org.example.apssolution.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.example.apssolution.domain.entity.Product;
import org.example.apssolution.domain.entity.Tool;
import org.example.apssolution.domain.entity.ToolCategory;
import org.example.apssolution.dto.request.ParseXlsRequest;
import org.example.apssolution.dto.request.product.UpsertProductRequest;
import org.example.apssolution.dto.request.tool.ParseToolXlsResponse;
import org.example.apssolution.dto.request.tool.UpsertToolRequest;
import org.example.apssolution.dto.response.product.ParseProductXlsResponse;
import org.example.apssolution.dto.response.product.ProductListResponse;
import org.example.apssolution.dto.response.product.ProductResponse;
import org.example.apssolution.dto.response.product.UpsertProductResponse;
import org.example.apssolution.dto.response.tool.UpsertToolResponse;
import org.example.apssolution.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/products")
public class ProductController {
    final ProductRepository productRepository;

    @Transactional
    @PutMapping // 품목 벌크 수정
    public ResponseEntity<?> upsertTools(@RequestBody @Valid UpsertProductRequest upr,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            FieldError fe = bindingResult.getFieldError();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fe.getDefaultMessage());
        }

        List<Product> myProducts = productRepository.findAll();
        List<String> targetIds = upr.getProducts()
                .stream().map(UpsertProductRequest.Item::getProductId).toList();
        List<Product> notContainsTools = myProducts.stream()
                .filter(t -> !targetIds.contains(t.getId())).toList();


        List<Product> upsertTools = upr.getProducts().stream().map(item -> {
            return Product.builder()
                    .id(item.getProductId())
                    .name(item.getName())
                    .description(item.getDescription()).build();
        }).toList();

        productRepository.deleteAll(notContainsTools);
        productRepository.saveAll(upsertTools);

        int delete = notContainsTools.size();
        int update = myProducts.size() - delete;
        int created = upsertTools.size() - update;

        return ResponseEntity.status(HttpStatus.CREATED).
                body(UpsertProductResponse.builder()
                        .created(created).deleted(delete).updated(update).build());
    }


    @GetMapping // 품목 전체 조회
    public ResponseEntity<?> getProducts() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ProductListResponse.builder().products(productRepository.findAll()).build());
    }


    @GetMapping("/{productId}") // 품목 상세 조회
    public ResponseEntity<?> getProduct(@PathVariable String productId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ProductResponse.builder().product(productRepository.findById(productId).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "품목 정보를 불러올 수 없습니다."))).build());
    }


    @PostMapping("/xls/parse") // 품목 엑셀파일 파싱
    public ResponseEntity<?> parseProductXls(@ModelAttribute ParseXlsRequest pxr) {
        ParseProductXlsResponse resp = new ParseProductXlsResponse();
        try (InputStream is = pxr.file().getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> iterator = sheet.rowIterator();
            if (!iterator.hasNext()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "엑셀 파일이 비어 있습니다");
            }
            iterator.next();
            DataFormatter formatter = new DataFormatter(); // 무조건 다 String 으로 받게 해주는 포멧터

            List<Product> products = new ArrayList<>();
            while (iterator.hasNext()) {
                Row row = iterator.next();

                String productId = formatter.formatCellValue(row.getCell(0));
                String productName = formatter.formatCellValue(row.getCell(1));
                String description = formatter.formatCellValue(row.getCell(2));
                boolean active = Boolean.parseBoolean(formatter.formatCellValue(row.getCell(3)));

                products.add(Product.builder()
                        .id(productId)
                        .name(productName)
                        .description(description)
                        .active(active)
                        .build());
            }
            resp.setProducts(products);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "엑셀 파일을 읽을 수 없습니다");
        }

        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }
}
