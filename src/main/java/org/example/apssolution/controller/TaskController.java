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
import org.example.apssolution.dto.request.task.TaskEditRequest;
import org.example.apssolution.dto.request.task.UpsertTaskRequest;
import org.example.apssolution.dto.response.task.ParseTaskXlsResponse;
import org.example.apssolution.dto.response.task.TaskListResponse;
import org.example.apssolution.dto.response.task.TaskResponse;
import org.example.apssolution.dto.response.task.UpsertTaskResponse;
import org.example.apssolution.repository.ProductRepository;
import org.example.apssolution.repository.TaskRepository;
import org.example.apssolution.repository.ToolCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/tasks")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Task", description = "작업 공정 관리 API")
public class TaskController {
    final TaskRepository taskRepository;
    final ProductRepository productRepository;
    final ToolCategoryRepository toolCategoryRepository;

    @Transactional
    @PutMapping // 작업 공정 벌크 수정
    @Operation(
            summary = "해당 품목의 하위 작업 공정 벌크 수정",
            description = """
                    전달된 작업 목록을 기준으로 전체 작업 데이터를 수정
                    
                    - 요청에 포함된 작업은 생성 또는 수정
                    - 요청에 포함되지 않은 기존 작업은 삭제
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "작업 공정 동기화 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpsertTaskResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 데이터 검증 실패 (필수값 누락, 형식 오류 등)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 품목 또는 카테고리"
            )
    })
    public ResponseEntity<?> upsertTasks(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "동기화할 전체 작업 공정 목록",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpsertTaskRequest.class))
            )
            @RequestBody @Valid UpsertTaskRequest utr,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            FieldError fe = bindingResult.getFieldError();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fe.getDefaultMessage());
        }

        List<Task> myTasks = taskRepository.findAll();
        List<String> targetIds = utr.getTasks()
                .stream().map(UpsertTaskRequest.Item::getTaskId).toList();
        List<Task> notContainsTasks = myTasks.stream()
                .filter(t -> !targetIds.contains(t.getId())).toList();


        List<Task> upsertTasks = utr.getTasks().stream().map(item -> {
            return Task.builder()
                    .id(item.getTaskId())
                    .product(productRepository.findById(item.getProductId()).orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "알 수 없는 품목입니다.")))
                    .toolCategory(toolCategoryRepository.findById(item.getCategoryId()).orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "알 수 없는 카테고리입니다.")))
                    .seq(item.getSeq())
                    .name(item.getName())
                    .description(item.getDescription())
                    .duration(item.getDuration()).build();
        }).toList();

        taskRepository.deleteAll(notContainsTasks);
        taskRepository.saveAll(upsertTasks);

        int delete = notContainsTasks.size();
        int update = myTasks.size() - delete;
        int created = upsertTasks.size() - update;

        return ResponseEntity.status(HttpStatus.OK).
                body(UpsertTaskResponse.builder()
                        .created(created).deleted(delete).updated(update).build());
    }


    @GetMapping // 작업 전체 조회
    @Operation(
            summary = "작업 전체 조회",
            description = "등록된 전체 작업 공정 목록 조회"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskListResponse.class),
                            examples = @ExampleObject(
                                    name = "제빵 공정 예시",
                                    value = """
                                            {
                                              "tasks": [
                                                {
                                                  "id": "TASK_DOUGH_001",
                                                  "productId": "BREAD_BAGUETTE",
                                                  "toolCategoryId": "MIXER",
                                                  "seq": 1,
                                                  "name": "반죽 혼합",
                                                  "description": "밀가루, 물, 이스트 혼합",
                                                  "duration": 15
                                                },
                                                {
                                                  "id": "TASK_FERMENT_001",
                                                  "productId": "BREAD_BAGUETTE",
                                                  "toolCategoryId": "FERMENTER",
                                                  "seq": 2,
                                                  "name": "1차 발효",
                                                  "description": "온도 28도에서 발효",
                                                  "duration": 60
                                                },
                                                {
                                                  "id": "TASK_BAKE_001",
                                                  "productId": "BREAD_BAGUETTE",
                                                  "toolCategoryId": "OVEN",
                                                  "seq": 3,
                                                  "name": "굽기",
                                                  "description": "220도 오븐에서 굽기",
                                                  "duration": 25
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<?> getTasks() {
        List<Task> allTasks = taskRepository.findAll();

        // 엔티티 -> DTO 변환
        List<TaskListResponse.TaskItem> taskItems = allTasks.stream()
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

        return ResponseEntity.ok(TaskListResponse.builder().tasks(taskItems).build());
    }


    @GetMapping("/{taskId}") // 작업 상세조회
    @Operation(
            summary = "작업 상세 조회",
            description = "작업 ID 기준 단일 작업 조회"
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponse.class),
                            examples = @ExampleObject(
                                    name = "제빵 작업 예시",
                                    value = """
                                            {
                                              "task": {
                                                "id": "TASK_BAKE_001",
                                                "product": {
                                                  "id": "BREAD_BAGUETTE"
                                                },
                                                "toolCategory": {
                                                  "id": "OVEN"
                                                },
                                                "seq": 3,
                                                "name": "굽기",
                                                "description": "220도 오븐에서 25분간 굽기",
                                                "duration": 25
                                              }
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "작업 없음"
            )
    })
    public ResponseEntity<?> getTask(@PathVariable("taskId") String taskId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(TaskResponse.builder().task(taskRepository.findById(taskId).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "작업 정보를 불러올 수 없습니다."))).build());
    }


    @PostMapping(value = "/xls/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "작업 엑셀 파싱",
            description = """
                    엑셀 파일 업로드 후 작업 공정 데이터 파싱 수행.
                    
                    - 엑셀 첫 번째 시트 기준 파싱
                    - 첫 행은 헤더로 간주하고 제외
                    - 각 행은 하나의 작업 공정 데이터로 변환
                    - 파싱 성공 시 DB 저장 없이 결과만 반환
                    """
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "200",
                    description = "엑셀 파싱 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ParseTaskXlsResponse.class),
                            examples = @ExampleObject(
                                    name = "제빵 공정 엑셀 파싱 예시",
                                    value = """
                                            {
                                              "tasks": [
                                                {
                                                  "id": "TASK_DOUGH_001",
                                                  "productId": "BREAD_BAGUETTE",
                                                  "toolCategoryId": "MIXER",
                                                  "seq": 1,
                                                  "name": "반죽 혼합",
                                                  "description": "밀가루, 물, 이스트 혼합",
                                                  "duration": 15
                                                },
                                                {
                                                  "id": "TASK_FERMENT_001",
                                                  "productId": "BREAD_BAGUETTE",
                                                  "toolCategoryId": "FERMENTER",
                                                  "seq": 2,
                                                  "name": "1차 발효",
                                                  "description": "온도 28도에서 발효",
                                                  "duration": 60
                                                },
                                                {
                                                  "id": "TASK_BAKE_001",
                                                  "productId": "BREAD_BAGUETTE",
                                                  "toolCategoryId": "OVEN",
                                                  "seq": 3,
                                                  "name": "굽기",
                                                  "description": "220도 오븐에서 25분간 굽기",
                                                  "duration": 25
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "엑셀 형식 오류 또는 파일 읽기 실패"
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 품목 또는 카테고리"
            )
    })
    public ResponseEntity<?> parseTaskXls(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "작업 공정 정보가 포함된 엑셀 파일 업로드",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ParseXlsRequest.class)
                    )
            )
            @ModelAttribute ParseXlsRequest pxr) {

        try (InputStream is = pxr.file().getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.rowIterator();

            if (!iterator.hasNext()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "엑셀 파일이 비어 있습니다."
                );
            }

            // 헤더 스킵
            iterator.next();

            DataFormatter formatter = new DataFormatter();
            List<Task> tasks = new ArrayList<>();

            while (iterator.hasNext()) {
                Row row = iterator.next();

                String taskId = formatter.formatCellValue(row.getCell(0));
                String productId = formatter.formatCellValue(row.getCell(1));
                String categoryId = formatter.formatCellValue(row.getCell(2));
                int seq = Integer.parseInt(formatter.formatCellValue(row.getCell(3)));
                String name = formatter.formatCellValue(row.getCell(4));
                String description = formatter.formatCellValue(row.getCell(5));
                int duration = Integer.parseInt(formatter.formatCellValue(row.getCell(6)));

                tasks.add(Task.builder()
                        .id(taskId)
                        .product(productRepository.findById(productId).orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "알 수 없는 품목 정보가 존재합니다."
                                )))
                        .toolCategory(toolCategoryRepository.findById(categoryId).orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "알 수 없는 카테고리가 존재합니다."
                                )))
                        .seq(seq)
                        .name(name)
                        .description(description)
                        .duration(duration)
                        .build());
            }

            List<ParseTaskXlsResponse.ParseTaskItem> items =
                    tasks.stream()
                            .sorted(Comparator.comparingInt(Task::getSeq))
                            .map(t -> ParseTaskXlsResponse.ParseTaskItem.builder()
                                    .id(t.getId())
                                    .productId(t.getProduct().getId())
                                    .toolCategoryId(t.getToolCategory().getId())
                                    .seq(t.getSeq())
                                    .name(t.getName())
                                    .description(t.getDescription())
                                    .duration(t.getDuration())
                                    .build())
                            .toList();

            return ResponseEntity.ok(
                    ParseTaskXlsResponse.builder()
                            .tasks(items)
                            .build()
            );

        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "엑셀 파일을 읽을 수 없습니다."
            );
        }
    }


    @Operation(
            summary = "작업 단건 수정",
            description = "작업 ID 기준으로 작업 정보 부분 수정. 요청 바디에 포함된 값만 변경."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "작업 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "task": {
                                        "id": "TASK-001",
                                        "name": "반죽 작업",
                                        "description": "1차 반죽 공정",
                                        "seq": 1,
                                        "duration": 45,
                                        "product": {
                                                    "id": "BRD-01",
                                                    "name": "바게트",
                                                    "description": "겉은 바삭하고 속은 촉촉한 프랑스 전통 빵",
                                                    "active": true,
                                                    "createdAt": "2026-01-10T04:00:00"
                                                  },
                                        "toolCategory": {
                                          "id": "TOOL-01",
                                          "name": "반죽기"
                                        }
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "작업 / 품목 / 도구 카테고리를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-01-27T15:10:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "작업을 찾을 수 없습니다.",
                                      "path": "/api/tasks/TASK-999"
                                    }
                                    """)
                    )
            )
    })
    @PatchMapping("/{taskId}")
    public ResponseEntity<?> editTask(@PathVariable("taskId") String taskId,
                                      @RequestBody TaskEditRequest ter) {
        Task task = taskRepository.findById(taskId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "작업을 찾을 수 없습니다."));
        if (ter.getProductId() != null) {
            task.setProduct(productRepository.findById(ter.getProductId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "품목을 찾을 수 없습니다.")));
        }
        if (ter.getToolCategoryId() != null) {
            task.setToolCategory(toolCategoryRepository.findById(ter.getToolCategoryId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "도구 카테고리를 찾을 수 없습니다.")));
        }
        if (ter.getSeq() != null) {
            task.setSeq(ter.getSeq());
        }
        if (ter.getName() != null) {
            task.setName(ter.getName());
        }
        if (ter.getDescription() != null) {
            task.setDescription(ter.getDescription());
        }
        if (ter.getDuration() != null) {
            task.setDuration(ter.getDuration());
        }
        taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.OK).body(TaskResponse.builder().task(task).build());
    }

}
