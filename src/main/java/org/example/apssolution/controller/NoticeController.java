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
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Notice;
import org.example.apssolution.domain.entity.NoticeComment;
import org.example.apssolution.domain.enums.Role;
import org.example.apssolution.dto.request.notice.CreateCommentRequest;
import org.example.apssolution.dto.request.notice.CreateNoticeRequest;
import org.example.apssolution.dto.request.notice.EditNoticeRequest;
import org.example.apssolution.dto.response.notice.*;
import org.example.apssolution.repository.NoticeCommentRepository;
import org.example.apssolution.repository.NoticeRepository;
import org.example.apssolution.service.notice.CreateNoticeService;
import org.example.apssolution.service.notice.DeleteNoticeService;
import org.example.apssolution.service.notice.EditNoticeService;
import org.example.apssolution.service.notice.SearchNoticeService;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notice", description = "공지사항 등록, 조회, 수정, 삭제 및 검색 API")
public class NoticeController {

    final SearchNoticeService searchNoticeService;
    final CreateNoticeService createNoticeService;
    final EditNoticeService editNoticeService;
    final DeleteNoticeService deleteNoticeService;
    final NoticeRepository noticeRepository;
    final NoticeCommentRepository noticeCommentRepository;

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
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NoticeListResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "notices": [
                                {
                                  "id": 101,
                                  "writer": {
                                    "id": "admin01",
                                    "name": "관리자",
                                    "role": "ADMIN",
                                    "profileImageUrl": "profile.png"
                                  },
                                  "title": "[SCN-001] 1월 4주차 생산 작업 배포",
                                  "content": "작업 일정 공유드립니다.",
                                  "createdAt": "2026-01-20T09:00:00"
                                }
                              ]
                            }
                            """)
            )
    )
    public ResponseEntity<?> getNotice() {
        return ResponseEntity.status(HttpStatus.OK).body(
                NoticeListResponse.builder()
                        .notices(noticeRepository.findAll().stream()
                                .filter(f -> f.getWriter().getRole() != Role.WORKER)
                                .map(n -> {
                                    int count = noticeCommentRepository.countByNoticeId(n.getId());
                                    return NoticeListResponse.from(n, count);
                                })
                                .toList())
                        .build()
        );
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
    public ResponseEntity<NoticeActionResponse> deleteNotice(@PathVariable Long noticeId,
                                                             @RequestAttribute("account") Account me) {
        deleteNoticeService.delete(noticeId, me);

        return ResponseEntity.ok(NoticeActionResponse.builder().success(true).message("공지 삭제 완료")
                .noticeId(noticeId).build());
    }

    @GetMapping("/{noticeId}")  // 공지사항 상세 조회
    @Operation(
            summary = "공지사항 상세 조회",
            description = "공지사항 ID 기준 상세 정보 조회"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoticeDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "noticeId": 12,
                                      "title": "2월 생산 스케줄 공유",
                                      "content": "이번 달 제빵 라인 점검 일정 안내",
                                      "writer": {
                                        "id": "emp01",
                                        "name": "김지훈",
                                        "role": "ADMIN",
                                        "profileImageUrl": "/images/profile/emp01.png"
                                      },
                                      "scenarioId": "SCN-2026-02",
                                      "createdAt": "2026-02-01T09:00:00",
                                      "attachments": [
                                        {
                                          "fileName": "production-plan.xlsx",
                                          "fileUrl": "/files/notices/12/plan.xlsx",
                                          "fileType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                        },
                                        {
                                          "fileName": "line-checklist.pdf",
                                          "fileUrl": "/files/notices/12/checklist.pdf",
                                          "fileType": "application/pdf"
                                        }
                                      ]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "공지사항 없음")
    })
    public ResponseEntity<NoticeDetailResponse> getNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(NoticeDetailResponse.from(noticeRepository.findById(noticeId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."))));
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


    @Operation(
            summary = "공지사항 첨부파일 다운로드",
            description = "공지사항에 첨부된 파일을 다운로드함. path 파라미터에는 파일의 상대 경로를 전달해야 함."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "파일 다운로드 성공",
                    content = @Content(
                            mediaType = "application/octet-stream",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "루트 경로를 벗어난 잘못된 파일 경로",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "잘못된 파일 경로"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "파일을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "파일 없음"
                                    }
                                    """)
                    )
            )
    })
    @GetMapping("/files/download")
    public ResponseEntity<?> downloadFile(@RequestParam String path) throws MalformedURLException {

        // 1️⃣ 파일 저장 루트 고정
        Path rootPath = Paths.get(System.getProperty("user.home"), "apssolution", "notices")
                .toAbsolutePath()
                .normalize();

        // 2️⃣ 요청으로 들어온 상대경로 붙이기
        Path targetPath = rootPath.resolve(path).normalize();

        // 3️⃣ 루트 밖으로 탈출했는지 검사 (보안 핵심)
        if (!targetPath.startsWith(rootPath)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "잘못된 파일 경로");
        }

        UrlResource resource = new UrlResource(targetPath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일 없음");
        }

        String encodedName = UriUtils.encode(resource.getFilename(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedName + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(resource);
    }

    // ============================================================

    @GetMapping("/community") // 직원게시판 전체 조회
    @Operation(
            summary = "직원 게시판 전체 조회",
            description = "시스템에 등록된 모든 공지사항 목록 조회함."
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NoticeListResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "notices": [
                                {
                                  "id": 101,
                                  "writer": {
                                    "id": "worker01",
                                    "name": "사원A",
                                    "role": "WORKER",
                                    "profileImageUrl": "profile.png"
                                  },
                                  "title": "OO사거리 교통사고 났네요",
                                  "content": "OO사거리 교통사고 났으니, 다들 우회해서 오세요",
                                  "createdAt": "2026-01-20T09:00:00"
                                }
                              ]
                            }
                            """)
            )
    )
    public ResponseEntity<?> getCommunities(@RequestAttribute Account account) {
        if (account.getRole() != Role.WORKER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "잘못된 접근입니다.");
        }

        List<NoticeListResponse.NoticeInfo> summaries = noticeRepository.findAll().stream()
                .filter(f -> f.getWriter().getRole() == Role.WORKER)
                .map(notice -> {
                    // 각 게시글의 댓글 개수 조회
                    int commentCount = noticeCommentRepository.countByNoticeId(notice.getId());

                    // 개수를 포함하여 DTO 생성 (NoticeListResponse.from 메서드에 인자 추가 필요)
                    return NoticeListResponse.from(notice, commentCount);
                })
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(
                NoticeListResponse.builder()
                        .notices(noticeRepository.findAll().stream()
                                .filter(f -> f.getWriter().getRole() == Role.WORKER)
                                .map(n -> {
                                    // ✅ 각 공지사항마다 댓글 개수를 카운트
                                    int count = noticeCommentRepository.countByNoticeId(n.getId());
                                    return NoticeListResponse.from(n, count); // ✅ 수정된 from 호출
                                })
                                .toList())
                        .build()
        );
    }


    @GetMapping("/community/{noticeId}")  // 직원 게시판 글 상세 조회
    @Operation(
            summary = "공지사항 상세 조회",
            description = "공지사항 ID 기준 상세 정보 조회"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoticeDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "noticeId": 12,
                                      "title": "2월 생산 스케줄 공유",
                                      "content": "이번 달 제빵 라인 점검 일정 안내",
                                      "writer": {
                                        "id": "emp01",
                                        "name": "김지훈",
                                        "role": "WORKER",
                                        "profileImageUrl": "/images/profile/emp01.png"
                                      },
                                      "scenarioId": "SCN-2026-02",
                                      "createdAt": "2026-02-01T09:00:00",
                                      "attachments": [
                                        {
                                          "fileName": "production-plan.xlsx",
                                          "fileUrl": "/files/notices/12/plan.xlsx",
                                          "fileType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                        },
                                        {
                                          "fileName": "line-checklist.pdf",
                                          "fileUrl": "/files/notices/12/checklist.pdf",
                                          "fileType": "application/pdf"
                                        }
                                      ]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "공지사항 없음")
    })
    public ResponseEntity<NoticeDetailResponse> getCommunity(@PathVariable Long noticeId,
                                                             @RequestAttribute Account account) {
        if (account.getRole() != Role.WORKER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "잘못된 접근입니다.");
        }
        return ResponseEntity.ok(NoticeDetailResponse.from(noticeRepository.findById(noticeId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."))));
    }

    @GetMapping("/{noticeId}/comments")     // 게시글 댓글 조회
    @Operation(summary = "게시글 댓글 목록 조회")
    public ResponseEntity<?> getComments(@PathVariable Long noticeId) {
        List<NoticeComment> comments = noticeCommentRepository.findByNoticeIdOrderByCreatedAtAsc(noticeId);

        // NoticeCommentResponse.Comment 리스트로 변환해서 반환
        return ResponseEntity.ok(comments.stream()
                .map(NoticeCommentResponse::from)
                .toList());
    }


    @PostMapping("/{noticeId}/comments")
    @Operation(
            summary = "공지사항 댓글 작성",
            description = """
                공지사항에 댓글 또는 대댓글 작성
                
                - commentId가 없으면 일반 댓글 생성
                - commentId가 있으면 해당 댓글의 대댓글 생성
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "댓글 작성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NoticeCommentResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "comment": {
                                    "id": 15,
                                    "noticeId": 3,
                                    "writerId": "EMP001",
                                    "content": "이 일정으로 진행하면 될 것 같습니다.",
                                    "parentCommentId": 12,
                                    "createdAt": "2026-01-28T18:30:00"
                                  }
                                }
                                """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패 (내용 누락 등)"),
            @ApiResponse(responseCode = "404", description = "공지사항 또는 부모 댓글을 찾을 수 없음")
    })
    public ResponseEntity<?> createComment(
            @Parameter(hidden = true) @RequestAttribute Account account,
            @Parameter(description = "공지사항 ID", example = "3")
            @PathVariable Long noticeId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "댓글 또는 대댓글 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateCommentRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                  "content": "이 일정으로 진행하면 될 것 같습니다.",
                                  "commentId": 12
                                }
                                """))
            )
            @RequestBody @Valid CreateCommentRequest ccr,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            FieldError fe = bindingResult.getFieldError();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fe.getDefaultMessage());
        }

        NoticeComment comment = NoticeComment.builder()
                .notice(noticeRepository.findById(noticeId).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.")))
                .writer(account)
                .content(ccr.getContent())
                .build();

        if (ccr.getCommentId() != null) {
            comment.setParent(noticeCommentRepository.findById(ccr.getCommentId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다.")));
        }

        noticeCommentRepository.save(comment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NoticeCommentResponse.builder()
                        .comment(NoticeCommentResponse.from(comment))
                        .build());
    }

    @Operation(
            summary = "공지사항 댓글 삭제",
            description = "공지사항에 작성된 댓글 삭제. 작성자 본인만 삭제 가능"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (작성자 아님)"),
            @ApiResponse(responseCode = "404", description = "댓글 없음 or 게시물 불일치")
    })
    @DeleteMapping("/{noticeId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId,
                                           @PathVariable Long noticeId,
                                           @RequestAttribute Account account) {
        NoticeComment comment = noticeCommentRepository.findById(commentId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."));
        if(!account.getId().equals(comment.getWriter().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }else if(!comment.getNotice().getId().equals(noticeId)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 게시물의 댓글이 아닙니다.");
        }

        noticeCommentRepository.delete(comment);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }



}
