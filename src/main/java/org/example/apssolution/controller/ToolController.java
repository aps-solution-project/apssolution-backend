package org.example.apssolution.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.example.apssolution.domain.entity.Tool;
import org.example.apssolution.domain.entity.ToolCategory;
import org.example.apssolution.dto.request.tool.CreateCategoryRequest;
import org.example.apssolution.dto.request.tool.UpsertToolRequest;
import org.example.apssolution.dto.request.ParseXlsRequest;
import org.example.apssolution.dto.request.tool.ParseToolXlsResponse;
import org.example.apssolution.dto.response.tool.*;
import org.example.apssolution.repository.ToolCategoryRepository;
import org.example.apssolution.repository.ToolRepository;
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
@RequestMapping("/api/tools")
public class ToolController {
    final ToolRepository toolRepository;
    final ToolCategoryRepository toolCategoryRepository;

    @PostMapping("/category")// 카테고리 추가
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
    public ResponseEntity<?> deleteCategory(@PathVariable("categoryId") String categoryId) {
        toolCategoryRepository.deleteById(categoryId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("/category") //카테고리 전체 조회
    public ResponseEntity<?> getCategory() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(CategoryListResponse.builder().categoryList(toolCategoryRepository.findAll()).build());
    }


    @Transactional
    @PutMapping //툴 벌크 수정
    public ResponseEntity<?> upsertTools(@RequestBody @Valid UpsertToolRequest ntr,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            FieldError fe = bindingResult.getFieldError();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fe.getDefaultMessage());
        }

        List<Tool> myTools = toolRepository.findAll();
        List<String> targetIds = ntr.getTools()
                .stream().map(UpsertToolRequest.Item::getToolId).toList();
        List<Tool> notContainsTools = myTools.stream()
                .filter(t -> !targetIds.contains(t.getId())).toList();


        List<Tool> upsertTools = ntr.getTools().stream().map(item -> {
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


        return ResponseEntity.status(HttpStatus.CREATED).
                body(UpsertToolResponse.builder()
                        .created(created).deleted(delete).updated(update).build());
    }


    @PostMapping("/xls/parse")// 툴 엑셀파일 파싱
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
    public ResponseEntity<?> getTools() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ToolListResponse.builder().tools(toolRepository.findAll()).build());
    }


    @GetMapping("/{toolId}") //툴 상세조회
    public ResponseEntity<?> getTool(@PathVariable("toolId") String toolId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ToolResponse.builder().tool(toolRepository.findById(toolId).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "도구 정보를 불러올 수 없습니다."))).build());
    }
}
