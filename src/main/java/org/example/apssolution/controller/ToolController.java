package org.example.apssolution.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.ToolCategory;
import org.example.apssolution.domain.enums.Role;
import org.example.apssolution.dto.request.CreateCategoryRequest;
import org.example.apssolution.dto.request.CreateToolRequest;
import org.example.apssolution.dto.response.tool.CreateCategoryResponse;
import org.example.apssolution.repository.AccountRepository;
import org.example.apssolution.repository.ToolCategoryRepository;
import org.example.apssolution.repository.ToolRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/tools")
public class ToolController {
    final ToolRepository toolRepository;
    final ToolCategoryRepository toolCategoryRepository;
    final AccountRepository accountRepository;

    @PostMapping("/category")// 툴 카테고리 추가
    public ResponseEntity<?> createCategory(@RequestAttribute String tokenId,
                                            @RequestBody @Valid CreateCategoryRequest ncr,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            FieldError fe = bindingResult.getFieldError();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fe.getDefaultMessage());
        }
        Account target = accountRepository.findById(tokenId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        if (target.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ADMIN 권한이 필요합니다");
        }


        if (toolCategoryRepository.existsById(ncr.getCategoryId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 카테고리입니다");
        }

        ToolCategory category = ncr.toToolCategory();

        toolCategoryRepository.save(category);

        return ResponseEntity.status(HttpStatus.CREATED).
                body(CreateCategoryResponse.builder().toolCategory(category).build());
    }

    @PostMapping //툴 단건 추가
    public ResponseEntity<?> createTool(@RequestBody @Valid CreateToolRequest ntr,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            FieldError fe = bindingResult.getFieldError();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fe.getDefaultMessage());
        }


        return null;
    }
}
