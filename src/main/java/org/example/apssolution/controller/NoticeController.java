package org.example.apssolution.controller;

import lombok.RequiredArgsConstructor;

import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.enums.Role;
import org.example.apssolution.dto.request.notice.CreateNoticeRequest;
import org.example.apssolution.dto.request.notice.EditNoticeRequest;
import org.example.apssolution.dto.response.notice.NoticeResponse;
import org.example.apssolution.repository.NoticeRepository;
import org.example.apssolution.repository.ScenarioRepository;
import org.example.apssolution.service.notice.CreateNoticeService;
import org.example.apssolution.service.notice.DeleteNoticeService;
import org.example.apssolution.service.notice.EditNoticeService;
import org.example.apssolution.service.notice.SearchNoticeService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

    private final SearchNoticeService searchNoticeService;
    private final CreateNoticeService createNoticeService;
    private final EditNoticeService editNoticeService;
    private final DeleteNoticeService deleteNoticeService;
    private final NoticeRepository noticeRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)    // 공지사항 등록
    public ResponseEntity<?> createNotice(@RequestPart(value = "request") CreateNoticeRequest request,
                                          @RequestPart(value = "attachment", required = false) List<MultipartFile> attachments,
                                          @RequestAttribute("account") Account me
    ) {
        createNoticeService.create(request, attachments, me);
        return ResponseEntity.ok().build();
    }

    @GetMapping // 공지사항 전제 조회
    public List<NoticeResponse> getNotice() {
        return noticeRepository.findAll().stream().map(NoticeResponse::from).toList();
    }

    @PutMapping(value = "/{noticeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editNotice(@PathVariable Long noticeId, @RequestPart("request") EditNoticeRequest request,
                                        @RequestPart(value = "attachment", required = false) List<MultipartFile> attachments,
                                        @RequestAttribute("account") Account me
    ) {
        editNoticeService.edit(noticeId, request, me);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{noticeId}")  // 공지사항 삭제
    public ResponseEntity<?> deleteNotice(@PathVariable Long noticeId, @RequestAttribute("account") Account me) {
        deleteNoticeService.delete(noticeId, me);
        return ResponseEntity.ok("공지 삭제 완료");
    }

    @GetMapping("/{noticeId}")  // 공지사항 상세 조회
    public ResponseEntity<?> getDetailNotice(@PathVariable Long noticeId) {
        return null;
    }

    @GetMapping("/search")   // 공지사항 검색
    public ResponseEntity<?> searchNotice(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String scenarioId) {
        List<NoticeResponse> result = searchNoticeService.search(keyword, scenarioId).stream()
                        .map(NoticeResponse::from).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/myNotice")    // 내가 쓴 글 찾기
    public ResponseEntity<?> myNotice(@RequestAttribute("account") Account me) {
        List<NoticeResponse> result = searchNoticeService.myNotices(me).stream()
                        .map(NoticeResponse::from).toList();

        return ResponseEntity.ok(result);
    }
}
