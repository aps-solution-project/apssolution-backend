package org.example.apssolution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.example.apssolution.domain.entity.Tool;
import org.example.apssolution.domain.entity.ToolCategory;
import org.example.apssolution.dto.request.tool.CreateCategoryRequest;
import org.example.apssolution.dto.request.tool.UpsertToolRequest;
import org.example.apssolution.dto.request.ParseXlsRequest;
import org.example.apssolution.dto.response.tool.ParseToolXlsResponse;
import org.example.apssolution.dto.response.tool.*;
import org.example.apssolution.repository.ToolCategoryRepository;
import org.example.apssolution.repository.ToolRepository;
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
import java.util.Iterator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/tools")
@SecurityRequirement(name="bearerAuth")
@Tag(name = "도구 API", description = "도구 및 도구 카테고리 관리 API")
public class ToolController {
    final ToolRepository toolRepository;
    final ToolCategoryRepository toolCategoryRepository;

    @PostMapping("/category")// 카테고리 추가
    @Operation(summary = "도구 카테고리 생성", description = "신규 도구 카테고리를 등록")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 카테고리")})
    public ResponseEntity<?> createCategory(@RequestBody @Valid CreateCategoryRequest ncr,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            FieldError fe = bindingResult.getFieldError();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fe.getDefaultMessage());
        }

        if (toolCategoryRepository.existsById(ncr.getCategoryId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 카테고리입니다");
        }

        ToolCategory category = ncr.toToolCategory();

        toolCategoryRepository.save(category);

        return ResponseEntity.status(HttpStatus.CREATED).
                body(CreateCategoryResponse.builder().toolCategory(category).build());
    }


    @DeleteMapping("/category/{categoryId}") // 카테고리 삭제
    @Operation(summary = "도구 카테고리 삭제", description = "카테고리 ID 기준으로 도구 카테고리 삭제")
    @Parameter(name = "categoryId", description = "카테고리 ID", required = true)
    @ApiResponse(responseCode = "204", description = "삭제 완료")
    public ResponseEntity<?> deleteCategory(@PathVariable("categoryId") String categoryId) {
        toolCategoryRepository.deleteById(categoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("/category") //카테고리 전체 조회
    @Operation(summary = "도구 카테고리 전체 조회", description = "등록된 모든 도구 카테고리 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<?> getCategory() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(CategoryListResponse.builder().categoryList(toolCategoryRepository.findAll()).build());
    }


    @Transactional
    @PutMapping //툴 벌크 수정
    @Operation(summary = "도구 벌크 동기화", description = "전달된 도구 목록 기준으로 데이터를 동기화. 목록에 없는 도구는 삭제, " +
        "존재하는 도구는 수정, 신규 도구는 생성")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "동기화 완료"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 오류"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")})
    public ResponseEntity<?> upsertTools(@RequestBody @Valid UpsertToolRequest utr,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            FieldError fe = bindingResult.getFieldError();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fe.getDefaultMessage());
        }

        List<Tool> myTools = toolRepository.findAll();
        List<String> targetIds = utr.getTools()
                .stream().map(UpsertToolRequest.Item::getToolId).toList();
        List<Tool> notContainsTools = myTools.stream()
                .filter(t -> !targetIds.contains(t.getId())).toList();


        List<Tool> upsertTools = utr.getTools().stream().map(item -> {
            return Tool.builder()
                    .id(item.getToolId())
                    .category(toolCategoryRepository.findById(item.getCategoryId()).orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "알 수 없는 카테고리입니다.")))
                    .description(item.getDescription()).build();
        }).toList();

        toolRepository.deleteAll(notContainsTools);
        toolRepository.saveAll(upsertTools);

        int delete = notContainsTools.size();
        int update = myTools.size() - delete;
        int created = upsertTools.size() - update;


        return ResponseEntity.status(HttpStatus.OK).
                body(UpsertToolResponse.builder()
                        .created(created).deleted(delete).updated(update).build());
    }


    @PostMapping(value = "/xls/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // 툴 엑셀파일 파싱
    @Operation(summary = "도구 엑셀 파일 파싱", description = "업로드된 엑셀 파일을 파싱하여 도구 목록 반환 (DB 반영 없음)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "파싱 성공"),
            @ApiResponse(responseCode = "400", description = "엑셀 파일 오류")})
    public ResponseEntity<?> parseToolXls(@ModelAttribute ParseXlsRequest pxr) {
        ParseToolXlsResponse resp = new ParseToolXlsResponse();
        try (InputStream is = pxr.file().getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            List<ToolCategory> categories = toolCategoryRepository.findAll();
            // 자바의 엑셀 객체
            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> iterator = sheet.rowIterator();
            if (!iterator.hasNext()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "엑셀 파일이 비어 있습니다");
            }
            iterator.next();
            DataFormatter formatter = new DataFormatter(); // 무조건 다 String 으로 받게 해주는 포멧터

            List<Tool> tools = new ArrayList<>();
            while (iterator.hasNext()) {
                Row row = iterator.next();

                String toolId = formatter.formatCellValue(row.getCell(0));
                String categoryId = formatter.formatCellValue(row.getCell(1));
                String description = formatter.formatCellValue(row.getCell(2));

                ToolCategory category = categories.stream()
                        .filter(c -> c.getId().equals(categoryId))
                        .findFirst()
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "알 수 없는 카테고리가 존재합니다."
                                )
                        );

                tools.add(Tool.builder()
                        .id(toolId)
                        .category(category)
                        .description(description)
                        .build());
            }
            resp.setTools(tools);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "엑셀 파일을 읽을 수 없습니다");
        }

        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }


    @GetMapping // 툴 전체 조회
    @Operation(summary = "도구 전체 조회", description = "등록된 모든 도구 목록 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<?> getTools() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ToolListResponse.builder().tools(toolRepository.findAll()).build());
    }


    @GetMapping("/{toolId}") //툴 상세조회
    @Operation(summary = "도구 상세 조회", description = "도구 ID 기준으로 도구 상세 정보 조회")
    @Parameter(name = "toolId", description = "도구 ID", required = true)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "도구 없음")})
    public ResponseEntity<?> getTool(@PathVariable("toolId") String toolId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ToolResponse.builder().tool(toolRepository.findById(toolId).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "도구 정보를 불러올 수 없습니다."))).build());
    }
}
