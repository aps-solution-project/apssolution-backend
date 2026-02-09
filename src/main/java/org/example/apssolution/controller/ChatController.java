package org.example.apssolution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.*;
import org.example.apssolution.domain.enums.MessageType;
import org.example.apssolution.dto.request.chat.CreateGroupChatRequest;
import org.example.apssolution.dto.request.chat.CreateMessageRequest;
import org.example.apssolution.dto.response.chat.*;
import org.example.apssolution.repository.*;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/chats")
public class ChatController {
    final AccountRepository accountRepository;
    final ChatRepository chatRepository;
    final ChatMemberRepository chatMemberRepository;
    final ChatAttachmentRepository chatAttachmentRepository;
    final ChatMessageRepository chatMessageRepository;
    final SimpMessagingTemplate template;

    @Operation(
            summary = "그룹 채팅방 생성 또는 기존 채팅방 조회",
            description = """
                    그룹 채팅방 생성 처리.
                    
                    - 요청 멤버 조합으로 이미 존재하는 채팅방이 있으면 해당 채팅방 정보 반환
                    - 존재하지 않으면 신규 그룹 채팅방 생성 후 반환
                    - 요청자(Account)는 자동으로 멤버에 포함됨
                    - 멤버 ID 조합을 기반으로 signature 생성하여 중복 방지 처리
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "신규 그룹 채팅방 생성 성공",
                    content = @Content(schema = @Schema(implementation = ChatGroupResponse.class))
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "이미 존재하는 그룹 채팅방 반환",
                    content = @Content(schema = @Schema(implementation = ChatGroupResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 데이터 검증 실패"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패"
            )
    })
    @PostMapping// 그룹 채팅방 생성 or 그 맴버로 존재하면 해당 채팅방 정보 반환
    public ResponseEntity<?> createGroupChat(@RequestAttribute Account account,
                                             @RequestBody @Valid CreateGroupChatRequest cgr,
                                             BindingResult bindingResult) {
        List<Account> chatMembers = accountRepository.findAllById(cgr.getMembers());
        if (chatMembers.stream().noneMatch(a -> a.getId().equals(account.getId()))) {
            chatMembers.add(account);
        }

        String signature = String.join(":", chatMembers.stream()
                .sorted(Comparator.comparing(Account::getId))
                .map(Account::getId).distinct().toList());

        Chat targetChat = chatRepository.findBySignature(signature);


        if (targetChat != null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ChatGroupResponse.from(targetChat, account, chatMembers));
        }

        Chat chat = Chat.builder()
                .owner(account)
                .roomName(cgr.getRoomName())
                .signature(signature)
                .build();
        chatRepository.save(chat);

        List<ChatMember> members = chatMembers.stream()
                .map(a -> ChatMember.builder()
                        .chat(chat)
                        .account(a)
                        .build())
                .toList();

        chatMemberRepository.saveAll(members);
        return ResponseEntity.status(HttpStatus.OK).body(ChatGroupResponse.from(chat, account, chatMembers));
    }


    @Operation(
            summary = "1:1 채팅방 시작 또는 기존 채팅방 조회",
            description = """
                    1:1 채팅방 처리 API.
                    
                    - 요청자(Account)와 대상 사용자(targetId) 조합으로 signature 생성
                    - 이미 존재하는 1:1 채팅방이 있으면 해당 채팅방 정보 반환
                    - 존재하지 않으면 신규 1:1 채팅방 생성 후 반환
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "이미 존재하는 1:1 채팅방 반환",
                    content = @Content(schema = @Schema(implementation = ChatDirectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "신규 1:1 채팅방 생성 성공",
                    content = @Content(schema = @Schema(implementation = ChatDirectResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "대상 사용자를 찾을 수 없음"
            ),
    })
    @PostMapping("/direct/{targetId}") // 1:1 채팅 시작 or 이미 해당 채팅방 있으면 정보 반환
    public ResponseEntity<?> directChat(@Parameter(
                                                description = "1:1 채팅을 시작할 대상 사용자 계정 ID",
                                                example = "user_123",
                                                required = true
                                        )
                                        @PathVariable("targetId") String targetId,
                                        @Parameter(hidden = true)
                                        @RequestAttribute Account account) {
        String signature = String.join(":", Stream.of(targetId, account.getId()).sorted().distinct().toList());

        Chat targetChat = chatRepository.findBySignature(signature);
        Account target = accountRepository.findById(targetId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "상대 사용자를 찾을 수 없습니다."));

        if (targetChat != null) {
            return ResponseEntity.status(HttpStatus.OK).body(ChatDirectResponse.from(targetChat, account, target));
        }

        Chat chat = Chat.builder()
                .owner(account)
                .signature(signature)
                .build();
        chatRepository.save(chat);

        List<ChatMember> members = List.of(
                ChatMember.builder()
                        .chat(chat)
                        .account(account)
                        .build(),
                ChatMember.builder()
                        .chat(chat)
                        .account(target)
                        .build());

        chatMemberRepository.saveAll(members);

        return ResponseEntity.status(HttpStatus.CREATED).body(ChatDirectResponse.from(chat, account, target));
    }

    @Operation(
            summary = "채팅 메시지 전송",
            description = """
                    채팅방에 메시지를 전송하는 API.
                    
                    - multipart/form-data 방식으로 요청
                    - TEXT 메시지 또는 FILE 메시지 전송 가능
                    - 채팅방 참여자만 메시지 전송 가능
                    - 파일 메시지의 경우 여러 파일 업로드 가능
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "메시지 전송 성공",
                    content = @Content(schema = @Schema(implementation = CreateMessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (내용 없음, 타입 오류 등)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "채팅방 참여자가 아님"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "채팅방을 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "파일 저장 중 서버 오류"
            )
    })
    @PostMapping(
            value = "/{chatId}/message",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Transactional //메시지 전송. formData로 보내야함!!!
    public ResponseEntity<?> sendMessage(@RequestAttribute Account account,
                                         @PathVariable String chatId,
                                         @ModelAttribute CreateMessageRequest cmr) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        boolean isMember = chat.getChatMembers() != null &&
                chat.getChatMembers().stream()
                        .anyMatch(m -> m.getAccount() != null &&
                                account.getId().equals(m.getAccount().getId()));

        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 채팅방에 참여자가 아닙니다.");
        }

        List<ChatAttachment> attachments = new ArrayList<>();
        ChatMessage message = null;
        switch (cmr.getType()) {
            case TEXT:
                if (cmr.getContent() == null || cmr.getContent().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "메시지 내용이 비어있습니다.");
                }
                message = ChatMessage.builder()
                        .chat(chat)
                        .talker(account)
                        .content(cmr.getContent())
                        .type(MessageType.TEXT)
                        .build();
                chatMessageRepository.save(message);
                break;
            case FILE:
                message = ChatMessage.builder()
                        .chat(chat)
                        .talker(account)
                        .type(MessageType.FILE)
                        .build();
                chatMessageRepository.save(message);

                if (cmr.getFiles() != null && !cmr.getFiles().isEmpty()) {

                    for (MultipartFile file : cmr.getFiles()) {
                        if (file.isEmpty()) continue;

                        Path uploadPath = Path.of(System.getProperty("user.home"), "apssolution", "chatAttachments", chatId, String.valueOf(message.getId()));

                        try {
                            Files.createDirectories(uploadPath);

                            String originalFileName = file.getOriginalFilename();
                            Path filePath = uploadPath.resolve(originalFileName);
                            file.transferTo(filePath.toFile());

                            String fileUrl = "/apssolution/chatAttachments/" + chatId + "/" + message.getId() + "/" + originalFileName;

                            ChatAttachment attachment = ChatAttachment.builder()
                                    .message(message)
                                    .fileName(originalFileName)
                                    .fileUrl(fileUrl)
                                    .fileType(file.getContentType())
                                    .build();

                            chatAttachmentRepository.save(attachment);
                            attachments.add(attachment);
                        } catch (IOException e) {
                            throw new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "파일 저장 중 오류가 발생했습니다."
                            );
                        }
                    }
                }
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 ContentType 입니다");
        }
        message.setAttachments(attachments);

        ChatMember chatMember = chat.getChatMembers().stream()
                .filter(m -> m.getAccount().getId().equals(account.getId())).findFirst().orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 채팅방에 권한이 없습니다."));
        chatMember.setLastActiveAt(LocalDateTime.now());
        chatMemberRepository.save(chatMember);

        template.convertAndSend("/topic/chat/" + chatId, ChatMessageResponse.from(message));
        chat.getChatMembers().forEach(member -> {
            template.convertAndSend("/topic/user/" + member.getAccount().getId(), Map.of("msg", "refresh"));
        });

        return ResponseEntity.status(HttpStatus.OK)
                .body(CreateMessageResponse.from(message));
    }

    @Operation(
            summary = "내가 참여 중인 채팅방 목록 조회",
            description = """
                    로그인한 사용자가 현재 참여 중인 모든 채팅방 목록을 조회합니다.
                    
                    - 채팅방 멤버 기준으로 조회
                    - 1:1 채팅방 및 그룹 채팅방 모두 포함
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 목록 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = ChatListResponse.class)
                    )
            ),
    })
    @GetMapping // 내가 소속된 채팅방 리스트 가져오기
    public ResponseEntity<?> getMyChats(@RequestAttribute Account account) {
        List<Chat> myChats = chatRepository.findAllByMemberAccountId(account.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ChatListResponse.from(myChats, account));
    }

    @Operation(
            summary = "채팅방 상세 조회",
            description = """
                    특정 채팅방의 상세 정보를 조회합니다.
                    
                    - 채팅방에 참여 중인 사용자만 조회 가능
                    - 이미 퇴장한 경우 조회 불가
                    - 조회 시 마지막 활성 시간(lastActiveAt) 갱신
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 상세 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = ChatDetailResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "채팅방 참여 권한 없음 또는 이미 퇴장한 사용자"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "채팅방을 찾을 수 없음"
            )
    })
    @GetMapping("/{chatId}") // 채팅방 상세보기
    public ResponseEntity<?> getChat(@RequestAttribute Account account,
                                     @PathVariable String chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        ChatMember chatMember = chatMemberRepository.findByChatIdAndAccountId(chatId, account.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 채팅방에 권한이 없습니다."));
        if (chatMember.getLeftAt() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 채팅방에 권한이 없습니다.");
        }
        chatMember.setLastActiveAt(LocalDateTime.now());
        chatMemberRepository.save(chatMember);
        return ResponseEntity.status(HttpStatus.OK).body(ChatDetailResponse.from(chat, account, chatMember));
    }

    @Operation(
            summary = "채팅 첨부 파일 다운로드",
            description = """
                    채팅 메시지에 첨부된 파일을 다운로드합니다.
                    
                    - 서버에 저장된 채팅 첨부 파일만 다운로드 가능
                    - 경로 조작(path traversal) 방지를 위해 루트 경로 검증 수행
                    - 파일은 attachment 형식으로 다운로드됨
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "파일 다운로드 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "잘못된 파일 경로 (허용되지 않은 경로 접근)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "파일이 존재하지 않거나 읽을 수 없음"
            )
    })
    @GetMapping("/files/download")
    public ResponseEntity<?> downloadFile(@RequestParam String path) throws MalformedURLException {

        // 1️⃣ 파일 저장 루트 고정
        Path rootPath = Paths.get(System.getProperty("user.home"), "apssolution", "chatAttachments")
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


    @Operation(
            summary = "채팅방 퇴장",
            description = """
                    채팅방에서 퇴장 처리합니다.
                    
                    - 채팅방 참여자만 퇴장 가능
                    - 퇴장 시 LEAVE 타입 시스템 메시지 생성
                    - 퇴장 시 채팅방 signature 갱신
                    - 마지막 참여자 퇴장 시 채팅방 삭제 가능
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "채팅방 퇴장 성공"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "채팅방 참여자가 아님"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "채팅방을 찾을 수 없음"
            )
    })
    @Transactional
    @DeleteMapping("/{chatId}/leave")
    public ResponseEntity<?> leaveChat(@RequestAttribute Account account,
                                       @PathVariable String chatId) {

        Chat chat = chatRepository.findById(chatId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        ChatMember chatMember = chatMemberRepository
                .findByChatIdAndAccountId(chatId, account.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 채팅방 참여자가 아닙니다."));

        chatMember.setLeftAt(LocalDateTime.now());
        chatMemberRepository.save(chatMember);

        chat.setSignature(String.join(":", Arrays.stream(chat.getSignature().split(":")).filter(id -> !id.equals(account.getId())).toList()));
        chatRepository.save(chat);

        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .talker(account)
                .type(MessageType.LEAVE)
                .build();

        chatMessageRepository.save(message);

        // 🔔 나가기 소켓 알림
        template.convertAndSend("/topic/chat/" + chatId, ChatMessageResponse.from(message));
//        chat.getChatMembers().forEach(member -> {
//            template.convertAndSend("/topic/user/" + member.getAccount().getId(), "refresh");
//        });

        // 🔥 채팅방에 아무도 안 남으면 방 삭제 여부 선택 가능
        if (chatMemberRepository.countByChat_IdAndLeftAtIsNull(chatId) <= 1) {
            chatRepository.delete(chat);
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "전체 안 읽은 메시지 수 조회",
            description = """
                    로그인한 사용자가 참여 중인 모든 채팅방을 기준으로
                    안 읽은 메시지의 총 개수를 조회합니다.
                    
                    - 본인이 보낸 메시지는 제외
                    - 마지막 활성 시간(lastActiveAt) 이후의 메시지만 집계
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 안 읽은 메시지 수 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = TotalUnreadCountResponse.class)
                    )
            )
    })
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadMessages(@RequestAttribute Account account) {
        List<Chat> myChats = chatRepository.findAllByMemberAccountId(account.getId());

        // 2️⃣ 전체 안 읽은 메시지 합산
        long totalUnread = 0;
        for (Chat chat : myChats) {
            ChatMember me = chat.getChatMembers().stream()
                    .filter(m -> m.getAccount().getId().equals(account.getId()))
                    .findFirst()
                    .orElse(null);

            if (me == null) continue;

            long unreadCount = chat.getChatMessages().stream()
                    .filter(m -> !m.getTalker().getId().equals(account.getId()))
                    .filter(m -> m.getTalkedAt().isAfter(me.getLastActiveAt()))
                    .count();

            totalUnread += unreadCount;
        }

        // 3️⃣ DTO 반환
        TotalUnreadCountResponse response = TotalUnreadCountResponse.builder()
                .totalUnreadCount(totalUnread)
                .build();

        return ResponseEntity.ok(response);

    }


}
