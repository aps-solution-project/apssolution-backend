package org.example.apssolution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.example.apssolution.domain.entity.Product;
import org.example.apssolution.domain.entity.Task;
import org.example.apssolution.dto.request.ParseXlsRequest;
import org.example.apssolution.dto.request.product.UpsertProductRequest;
import org.example.apssolution.dto.request.task.UpsertTaskRequest;
import org.example.apssolution.dto.response.product.ParseProductXlsResponse;
import org.example.apssolution.dto.response.product.ProductListResponse;
import org.example.apssolution.dto.response.product.ProductResponse;
import org.example.apssolution.dto.response.product.UpsertProductResponse;
import org.example.apssolution.dto.response.task.TaskListResponse;
import org.example.apssolution.dto.response.task.UpsertTaskResponse;
import org.example.apssolution.repository.ProductRepository;
import org.example.apssolution.repository.TaskRepository;
import org.example.apssolution.repository.ToolCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/products")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Product", description = "품목(Product) 관리 API")
public class ProductController {
    final ProductRepository productRepository;
    final TaskRepository taskRepository;
    final ToolCategoryRepository toolCategoryRepository;

    @Operation(
            summary = "품목 벌크 저장/수정",
            description = """
                전달된 품목 목록 기준 기존 데이터 동기화 처리.
                
                - 요청에 포함된 품목은 수정 또는 생성
                - 요청에 포함되지 않은 기존 품목은 삭제
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpsertProductResponse.class),
                            examples = @ExampleObject(
                                    name = "처리 결과 예시",
                                    value = """
                                        {
                                          "created": 2,
                                          "updated": 3,
                                          "deleted": 1
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "동기화할 품목 목록",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpsertProductRequest.class),
                    examples = @ExampleObject(
                            name = "제빵 품목 예시",
                            value = """
                                {
                                  "products": [
                                    {
                                      "productId": "BREAD_BAGUETTE",
                                      "name": "바게트",
                                      "description": "프랑스식 하드 브레드"
                                    },
                                    {
                                      "productId": "BREAD_CROISSANT",
                                      "name": "크루아상",
                                      "description": "버터 레이어 페이스트리"
                                    }
                                  ]
                                }
                                """
                    )
            )
    )
    @Transactional
    @PutMapping // 품목 벌크 수정
    public ResponseEntity<?> upsertProducts(@RequestBody @Valid UpsertProductRequest upr,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            FieldError fe = bindingResult.getFieldError();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fe.getDefaultMessage());
        }

        List<Product> myProducts = productRepository.findAll();
        List<String> targetIds = upr.getProducts()
                .stream().map(UpsertProductRequest.Item::getProductId).toList();
        List<Product> notContainsProducts = myProducts.stream()
                .filter(t -> !targetIds.contains(t.getId())).toList();


        List<Product> upsertProducts = upr.getProducts().stream().map(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseGet(() -> Product.builder()
                            .id(item.getProductId())
                            .active(true)
                            .build());

            product.setName(item.getName());
            product.setDescription(item.getDescription());

            return product;
        }).toList();

        productRepository.deleteAll(notContainsProducts);
        productRepository.saveAll(upsertProducts);

        int delete = notContainsProducts.size();
        int update = myProducts.size() - delete;
        int created = upsertProducts.size() - update;

        return ResponseEntity.status(HttpStatus.OK).
                body(UpsertProductResponse.builder()
                        .created(created).deleted(delete).updated(update).build());
    }


    @Operation(
            summary = "품목 작업 공정 벌크 수정",
            description = """
                    특정 품목에 속한 작업 공정을 요청 데이터 기준으로 일괄 수정함.
                    
                    - 요청에 포함된 기존 작업은 수정 처리
                    - taskId가 없는 항목은 신규 생성
                    - 요청에 포함되지 않은 기존 작업은 삭제
                    - 다른 품목에 속한 작업을 수정하려는 경우 오류 발생
                    """
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "200",
                    description = "작업 공정 벌크 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpsertTaskResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패 (필수값 누락, 형식 오류 등)"
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 품목 또는 작업 또는 카테고리"
            ),

            @ApiResponse(
                    responseCode = "409",
                    description = "다른 품목에 속한 작업을 수정하려는 경우 충돌 발생"
            )
    })
    @Transactional
    @PatchMapping("/{productId}/tasks")
    public ResponseEntity<?> upsertProductTasks(@io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                        description = "품목의 작업 공정을 일괄 생성 또는 수정합니다.",
                                                        required = true,
                                                        content = @Content(schema = @Schema(implementation = UpsertTaskRequest.class))
                                                ) @RequestBody @Valid UpsertTaskRequest utr,
                                                BindingResult bindingResult,
                                                @Parameter(
                                                        name = "productId",
                                                        description = "작업 공정을 수정할 대상 품목 ID",
                                                        required = true,
                                                        example = "PRODUCT_A"
                                                )
                                                @PathVariable String productId) {
        if (bindingResult.hasErrors()) {
            FieldError fe = bindingResult.getFieldError();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fe.getDefaultMessage());
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "알 수 없는 품목입니다."));

        List<Task> myProductTasks = taskRepository.findByProduct(product);

        List<String> requestTaskIds = utr.getTasks().stream()
                .map(UpsertTaskRequest.Item::getTaskId)
                .toList();

        if (!requestTaskIds.isEmpty()) {
            List<Task> foundTasks = taskRepository.findAllById(requestTaskIds);

            if (foundTasks.size() != requestTaskIds.size()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 작업이 포함되어 있습니다.");
            }

            for (Task task : foundTasks) {
                if (!task.getProduct().getId().equals(productId)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "다른 품목의 작업은 수정할 수 없습니다.");
                }
            }
        }

        List<Task> notContainsTasks = myProductTasks.stream()
                .filter(t -> requestTaskIds.stream().noneMatch(id -> id.equals(t.getId())))
                .toList();

        List<Task> upsertTasks = new ArrayList<>();

        for (UpsertTaskRequest.Item item : utr.getTasks()) {

            Task task;

            if (item.getTaskId() != null) {
                task = taskRepository.findById(item.getTaskId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "알 수 없는 작업입니다."));
            } else {
                task = new Task();
                task.setProduct(product);
            }

            task.setName(item.getName());
            task.setDescription(item.getDescription());
            task.setSeq(item.getSeq());
            task.setDuration(item.getDuration());

            task.setToolCategory(
                    toolCategoryRepository.findById(item.getCategoryId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "알 수 없는 카테고리입니다."))
            );

            upsertTasks.add(task);
        }

        taskRepository.deleteAll(notContainsTasks);
        taskRepository.saveAll(upsertTasks);

        int delete = notContainsTasks.size();
        int update = myProductTasks.size() - delete;
        int created = upsertTasks.size() - update;

        return ResponseEntity.ok(
                UpsertTaskResponse.builder()
                        .created(created)
                        .deleted(delete)
                        .updated(update)
                        .build()
        );
    }


    @Operation(
            summary = "품목 전체 조회",
            description = "등록된 모든 품목 목록을 조회."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "products": [
                                {
                                  "id": "BRD-01",
                                  "name": "바게트",
                                  "description": "겉은 바삭하고 속은 촉촉한 프랑스 전통 빵",
                                  "active": true,
                                  "createdAt": "2026-01-10T04:00:00"
                                },
                                {
                                  "id": "BRD-02",
                                  "name": "크루아상",
                                  "description": "버터 풍미가 가득한 페이스트리",
                                  "active": true,
                                  "createdAt": "2026-01-11T04:10:00"
                                }
                              ]
                            }
                            """)))
    @GetMapping // 품목 전체 조회
    public ResponseEntity<?> getProducts() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ProductListResponse.builder().products(productRepository.findAll()).build());
    }


    @Operation(
            summary = "품목 상세 조회",
            description = "품목 ID 기준 단일 품목 상세 정보 조회"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "product": {
                                        "id": "BRD-01",
                                        "name": "바게트",
                                        "description": "겉은 바삭하고 속은 촉촉한 프랑스 전통 빵",
                                        "active": true,
                                        "createdAt": "2026-01-10T04:00:00"
                                      }
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404", description = "품목을 찾을 수 없음")
    })
    @GetMapping("/{productId}") // 품목 상세 조회
    public ResponseEntity<?> getProduct(@PathVariable String productId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ProductResponse.builder().product(productRepository.findById(productId).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "품목 정보를 불러올 수 없습니다."))).build());
    }


    @Operation(
            summary = "품목 엑셀 파일 파싱",
            description = "업로드된 엑셀 파일 파싱 후 품목 데이터 미리보기 반환"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "파싱 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "products": [
                                        {
                                          "id": "BRD-03",
                                          "name": "초코 머핀",
                                          "description": "진한 초콜릿이 들어간 머핀",
                                          "active": true,
                                          "createdAt": null
                                        },
                                        {
                                          "id": "BRD-04",
                                          "name": "식빵",
                                          "description": "부드러운 기본 식빵",
                                          "active": false,
                                          "createdAt": 2026-01-25T09:30:00
                                        }
                                      ]
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "400", description = "파일 형식 오류 또는 읽기 실패")
    })
    @PostMapping(value = "/xls/parse", consumes = "multipart/form-data") // 품목 엑셀파일 파싱
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


    @Operation(
            summary = "품목별 작업 목록 조회",
            description = "품목 ID 기준 단일 품목 상세 정보 조회 - 공정 순서(seq) 기준으로 정렬하여 조회"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "tasks": [
                                {
                                  "id": "TASK-01",
                                  "seq": 1,
                                  "name": "반죽 혼합",
                                  "description": "밀가루, 물, 이스트 재료를 혼합",
                                  "duration": 20
                                },
                                {
                                  "id": "TASK-02",
                                  "seq": 2,
                                  "name": "1차 발효",
                                  "description": "온도와 습도를 맞춰 반죽 발효",
                                  "duration": 60
                                },
                                {
                                  "id": "TASK-03",
                                  "seq": 3,
                                  "name": "성형",
                                  "description": "반죽을 빵 모양으로 성형",
                                  "duration": 15
                                },
                                {
                                  "id": "TASK-04",
                                  "seq": 4,
                                  "name": "굽기",
                                  "description": "오븐에서 적정 온도로 굽기",
                                  "duration": 25
                                }
                              ]
                            }
                            """)
            ))
    @GetMapping("/{productId}/tasks") // 품목 아이디로 하위 작업 조회
    public ResponseEntity<?> getProductTasks(@PathVariable String productId) {
        // 1. 해당 품목의 작업을 조회 및 정렬
        List<TaskListResponse.TaskItem> taskItems = taskRepository.findAll().stream()
                .filter(t -> productId.equals(t.getProduct().getId()))
                .sorted((a, b) -> Integer.compare(a.getSeq(), b.getSeq()))
                // 2. ★ 핵심: Task 엔티티를 TaskItem DTO로 변환 ★
                .map(t -> TaskListResponse.TaskItem.builder()
                        .id(t.getId())
                        .productId(t.getProduct().getId())
                        .toolCategoryId(t.getToolCategory().getId())
                        .seq(t.getSeq())
                        .name(t.getName())
                        .description(t.getDescription())
                        .duration(t.getDuration())
                        .build())
                .toList();

        // 3. 변환된 DTO 리스트를 응답 바디에 담아 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(TaskListResponse.builder().tasks(taskItems).build());
    }
}
