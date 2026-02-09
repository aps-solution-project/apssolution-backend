package org.example.apssolution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.apssolution.dto.search.GlobalSearchResponse;
import org.example.apssolution.service.search.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    @Operation(
            summary = "통합 키워드 검색",
            description = """
                    입력한 키워드를 기준으로 시스템 전반을 통합 검색합니다.
                    
                    - 시나리오, 제품, 작업(Task), 설비(Tool), 공지사항을 동시에 검색
                    - 각 카테고리별 최대 3건까지 반환
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "통합 검색 성공",
                    content = @Content(
                            schema = @Schema(implementation = GlobalSearchResponse.class)
                    )
            )
    })
    @GetMapping // 키워드 전체 조회
    public ResponseEntity<?> search(@Parameter(
            description = "검색 키워드",
            example = "데니쉬",
            required = true
    )
                                    @RequestParam String keyword) {
        return ResponseEntity.ok(
                searchService.searchAll(keyword)
        );
    }
}
