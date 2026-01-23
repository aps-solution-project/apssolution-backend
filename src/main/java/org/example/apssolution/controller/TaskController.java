package org.example.apssolution.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.example.apssolution.domain.entity.Task;
import org.example.apssolution.dto.request.ParseXlsRequest;
import org.example.apssolution.dto.request.task.UpsertTaskRequest;
import org.example.apssolution.dto.response.task.ParseTaskXlsResponse;
import org.example.apssolution.dto.response.task.TaskListResponse;
import org.example.apssolution.dto.response.task.TaskResponse;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/tasks")
public class TaskController {
    final TaskRepository taskRepository;
    final ProductRepository productRepository;
    final ToolCategoryRepository toolCategoryRepository;

    @Transactional
    @PutMapping // 작업 공정 벌크 수정
    public ResponseEntity<?> upsertTasks(@RequestBody @Valid UpsertTaskRequest utr,
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
    public ResponseEntity<?> getTasks() {
        return ResponseEntity.status(HttpStatus.OK).body(TaskListResponse.builder().tasks(taskRepository.findAll()));
    }


    @GetMapping("/{taskId}") // 작업 상세조회
    public ResponseEntity<?> getTasks(@PathVariable("taskId") String taskId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(TaskResponse.builder().task(taskRepository.findById(taskId).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "작업 정보를 불러올 수 없습니다."))).build());
    }


    @PostMapping("/xls/parse") // 작업 엑셀파일 파싱
    public ResponseEntity<?> parseTaskXls(@ModelAttribute ParseXlsRequest pxr) {
        ParseTaskXlsResponse resp = new ParseTaskXlsResponse();
        try (InputStream is = pxr.file().getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> iterator = sheet.rowIterator();
            if (!iterator.hasNext()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "엑셀 파일이 비어 있습니다");
            }
            iterator.next();
            DataFormatter formatter = new DataFormatter(); // 무조건 다 String 으로 받게 해주는 포멧터

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
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "알 수 없는 품목 정보가 존재합니다.")))
                        .toolCategory(toolCategoryRepository.findById(categoryId).orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "알 수 없는 카테고리가 존재합니다.")))
                        .seq(seq)
                        .name(name)
                        .description(description)
                        .duration(duration)
                        .build());
            }
            resp.setTasks(tasks.stream().sorted(Comparator.comparingInt(Task::getSeq)).toList());
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "엑셀 파일을 읽을 수 없습니다");
        }

        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }

}
