package org.example.apssolution.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Notice;
import org.example.apssolution.dto.request.notice.CreateNoticeRequest;
import org.example.apssolution.dto.request.notice.EditNoticeRequest;
import org.example.apssolution.dto.response.notice.NoticeActionResponse;
import org.example.apssolution.dto.response.notice.NoticeDetailResponse;
import org.example.apssolution.dto.response.notice.NoticeSearchResponse;
import org.example.apssolution.repository.NoticeRepository;
import org.example.apssolution.service.notice.CreateNoticeService;
import org.example.apssolution.service.notice.DeleteNoticeService;
import org.example.apssolution.service.notice.EditNoticeService;
import org.example.apssolution.service.notice.SearchNoticeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
@SecurityRequirement(name="bearerAuth")
public class NoticeController {

    private final SearchNoticeService searchNoticeService;
    private final CreateNoticeService createNoticeService;
    private final EditNoticeService editNoticeService;
    private final DeleteNoticeService deleteNoticeService;
    private final NoticeRepository noticeRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)    // 공지사항 등록
    public ResponseEntity<NoticeActionResponse> createNotice(@ModelAttribute CreateNoticeRequest request,
                                                             @RequestAttribute("account") Account me) throws IOException {
        Long savedId = createNoticeService.create(request, me);

        return ResponseEntity.ok(NoticeActionResponse.builder().success(true).message("공지사항이 등록되었습니다.")
                .noticeId(savedId).build());
    }

    @GetMapping // 공지사항 전체 조회
    public List<NoticeActionResponse> getNotice() {
        return noticeRepository.findAll().stream()
                .map(notice -> NoticeActionResponse.builder().noticeId(notice.getId())
                        .title(notice.getTitle()).writerName(notice.getWriter().getName())
                        .success(true).build()).toList();
    }

    @PatchMapping("/{noticeId}") // 공지사항 수정
    public ResponseEntity<NoticeActionResponse> editNotice(@PathVariable Long noticeId,
                                                           @ModelAttribute EditNoticeRequest request,
                                                           @RequestAttribute("account") Account me) throws IOException {

        editNoticeService.edit(noticeId, request, me);

        return ResponseEntity.ok(NoticeActionResponse.builder().success(true).message("수정 완료")
                .noticeId(noticeId).build());
    }

    @DeleteMapping("/{noticeId}")  // 공지사항 삭제
    public ResponseEntity<NoticeActionResponse> deleteNotice(@PathVariable Long noticeId, @RequestAttribute("account") Account me) {
        deleteNoticeService.delete(noticeId, me);

        return ResponseEntity.ok(NoticeActionResponse.builder().success(true).message("공지 삭제 완료")
                .noticeId(noticeId).build());
    }

    @GetMapping("/{noticeId}")  // 공지사항 상세 조회
    public ResponseEntity<NoticeDetailResponse> getNotice(@PathVariable Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow();
        return ResponseEntity.ok(NoticeDetailResponse.from(notice));
    }

    @GetMapping("/search")   // 공지사항 검색
    public ResponseEntity<List<NoticeSearchResponse>> searchNotice(@RequestParam(required = false) String keyword,
                                                                   @RequestParam(required = false) String scenarioId) {
        List<NoticeSearchResponse> result = searchNoticeService.search(keyword, scenarioId).stream()
                .map(notice -> {
                    // 본문 요약 (50자)
                    String summary = notice.getContent();
                    if (summary != null && summary.length() > 50) {
                        summary = summary.substring(0, 50) + "...";
                    }

                    return NoticeSearchResponse.builder()
                            .noticeId(notice.getId())
                            .title(notice.getTitle())
                            .content(summary)
                            .writerName(notice.getWriter().getName())
                            .createdAt(notice.getCreatedAt())
                            // 시나리오가 있을 때만 정보 세팅
                            .scenarioId(notice.getScenario() != null ? notice.getScenario().getId() : null)
                            .scenarioTitle(notice.getScenario() != null ? notice.getScenario().getTitle() : "일반 공지")
                            .build();
                }).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/myNotice")    // 내가 쓴 글 조회
    public ResponseEntity<List<NoticeActionResponse>> myNotice(@RequestAttribute("account") Account me) {
        List<NoticeActionResponse> result = searchNoticeService.myNotices(me).stream()
                .map(notice -> NoticeActionResponse.builder().noticeId(notice.getId())
                        .title(notice.getTitle()).writerName(notice.getWriter().getName())
                        .success(true).build()).toList();

        return ResponseEntity.ok(result);
    }
}
