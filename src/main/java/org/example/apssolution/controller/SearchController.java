package org.example.apssolution.controller;

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

    @GetMapping
    public ResponseEntity<?> search(@RequestParam String keyword) {
        return ResponseEntity.ok(
                searchService.searchAll(keyword)
        );
    }
}
