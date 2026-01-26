package org.example.apssolution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notice", description = "공지사항 등록, 조회, 수정, 삭제 및 검색 API")
public class NoticeController {

    private final SearchNoticeService searchNoticeService;
    private final CreateNoticeService createNoticeService;
    private final EditNoticeService editNoticeService;
    private final DeleteNoticeService deleteNoticeService;
    private final NoticeRepository noticeRepository;

    @Operation(
            summary = "공지사항 등록",
            description = "공지사항 신규 등록 API. 제목, 내용, 시나리오 연결 여부 설정 및 첨부파일 업로드 처리함."
    )
    @ApiResponse(responseCode = "200", description = "등록 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NoticeActionResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "공지사항이 등록되었습니다.",
                              "noticeId": 101,
                              "title": "[SCN-001] 1월 4주차 생산 작업 배포",
                              "content": "시나리오 SCN-001 확정됨. 작업 일정 확인 요청함.",
                              "writerName": "관리자Id"
                            }
                            """
                    )))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoticeActionResponse> createNotice(@ModelAttribute CreateNoticeRequest request,
                                                             @RequestAttribute("account") Account me) throws IOException {
        Long savedId = createNoticeService.create(request, me);

        return ResponseEntity.ok(NoticeActionResponse.builder().success(true).message("공지사항이 등록되었습니다.")
                .noticeId(savedId).build());
    }

    @GetMapping // 공지사항 전체 조회
    @Operation(
            summary = "공지사항 전체 조회",
            description = "시스템에 등록된 모든 공지사항 목록 조회함."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NoticeActionResponse.class),
                    examples = @ExampleObject(value = """
                            [
                              {
                                "success": true,
                                "noticeId": 101,
                                "title": "[SCN-001] 1월 4주차 생산 작업 배포",
                                "writerName": "관리자"
                              },
                              {
                                "success": true,
                                "noticeId": 102,
                                "title": "설비 점검 일정 안내",
                                "writerName": "설비팀장"
                              }
                            ]
                            """)
            )
    )
    public List<NoticeActionResponse> getNotice() {
        return noticeRepository.findAll().stream()
                .map(notice -> NoticeActionResponse.builder().noticeId(notice.getId())
                        .title(notice.getTitle()).writerName(notice.getWriter().getName())
                        .success(true).build()).toList();
    }

    @PatchMapping("/{noticeId}") // 공지사항 수정
    @Operation(
            summary = "공지사항 수정",
            description = "기존 공지사항 내용 수정함. 작성자 본인 또는 관리자만 수정 가능."
    )
    @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NoticeActionResponse.class),
                    examples = @ExampleObject(value = """
                            {
                                  "success": true,
                                  "message": "수정 완료",
                                  "noticeId": 101
                            }
                            """)
            )
    )
    public ResponseEntity<NoticeActionResponse> editNotice(@PathVariable Long noticeId,
                                                           @ModelAttribute EditNoticeRequest request,
                                                           @RequestAttribute("account") Account me) throws IOException {
        editNoticeService.edit(noticeId, request, me);
        return ResponseEntity.ok(NoticeActionResponse.builder().success(true).message("수정 완료")
                .noticeId(noticeId).build());
    }

    @DeleteMapping("/{noticeId}")  // 공지사항 삭제
    @Operation(
            summary = "공지사항 삭제",
            description = "공지사항 삭제 처리함. 작성자 본인 또는 관리자만 삭제 가능."
    )
    @ApiResponse(responseCode = "200", description = "삭제 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NoticeActionResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "공지 삭제 완료",
                              "noticeId": 101
                            }
                            """)
            )
    )
    public ResponseEntity<NoticeActionResponse> deleteNotice(@PathVariable Long noticeId, @RequestAttribute("account") Account me) {
        deleteNoticeService.delete(noticeId, me);

        return ResponseEntity.ok(NoticeActionResponse.builder().success(true).message("공지 삭제 완료")
                .noticeId(noticeId).build());
    }

    @GetMapping("/{noticeId}")  // 공지사항 상세 조회
    @Operation(summary = "공지사항 상세 조회", description = "공지사항 ID 기준으로 상세 정보를 조회.")
    public ResponseEntity<NoticeDetailResponse> getNotice(@PathVariable Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow();
        return ResponseEntity.ok(NoticeDetailResponse.from(notice));
    }

    @GetMapping("/search")   // 공지사항 검색
    @Operation(
            summary = "공지사항 상세 조회",
            description = "공지사항 ID 기준 상세 정보 조회"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NoticeDetailResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "noticeId": 101,
                              "title": "[SCN-001] 1월 4주차 생산 작업 배포",
                              "content": "시나리오 SCN-001이 확정되었습니다. 1월 4주차 생산 일정이 확정되었으니 작업자분들은 배정된 공정을 확인 부탁드립니다.",
                              "writerName": "관리자",
                              "scenarioId": "SCN-001",
                              "createdAt": "2026-01-26T09:00:00",
                              "attachments": [
                                {
                                  "fileName": "production_schedule_week4.xlsx",
                                  "fileUrl": "/apssolution/notices/2c8fcec573cc4cacb1f61c54b0c5b263/JB_job_sample.xlsx",
                                  "fileType": "xlsx"
                                },
                                {
                                  "fileName": "process_guide.pdf",
                                  "fileUrl": "/apssolution/notices/2c8fcec573cc4cacb1f61c54b0c5b263/JB_job_sample.pdf",
                                  "fileType": "pdf"
                                }
                              ]
                            }
                            """)
            )
    )
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
    @Operation(
            summary = "내 공지사항 조회",
            description = "로그인한 사용자가 작성한 공지사항 목록 조회"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NoticeActionResponse.class),
                    examples = @ExampleObject(value = """
                            [
                              {
                                "success": true,
                                "noticeId": 96,
                                "title": "[SCN-001] 1월 4주차 생산 작업 배포",
                                "writerName": "관리자"
                              },
                              {
                                "success": true,
                                "noticeId": 97,
                                "title": "설비 점검 일정 안내",
                                "writerName": "관리자"
                              }
                            ]
                            """)
            )
    )
    public ResponseEntity<List<NoticeActionResponse>> myNotice(@RequestAttribute("account") Account me) {
        List<NoticeActionResponse> result = searchNoticeService.myNotices(me).stream()
                .map(notice -> NoticeActionResponse.builder().noticeId(notice.getId())
                        .title(notice.getTitle()).writerName(notice.getWriter().getName())
                        .success(true).build()).toList();

        return ResponseEntity.ok(result);
    }
}
