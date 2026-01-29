package org.example.apssolution.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.*;
import org.example.apssolution.domain.enums.MessageType;
import org.example.apssolution.dto.request.chat.CreateGroupChatRequest;
import org.example.apssolution.dto.request.chat.CreateMessageRequest;
import org.example.apssolution.dto.response.chat.*;
import org.example.apssolution.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
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

    @PostMapping("/direct/{targetId}") // 1:1 채팅 시작 or 이미 해당 채팅방 있으면 정보 반환
    public ResponseEntity<?> directChat(@PathVariable("targetId") String targetId,
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

    @Transactional
    @PostMapping("/{chatId}/message") //메시지 전송. formData로 보내야함!!!
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

                        Path uploadPath = Path.of(System.getProperty("user.home"), "apssolution", "chatAttachments", String.valueOf(message.getId()));

                        try {
                            Files.createDirectories(uploadPath);

                            String originalFileName = file.getOriginalFilename();
                            Path filePath = uploadPath.resolve(originalFileName);
                            file.transferTo(filePath.toFile());

                            String fileUrl = "/apssolution/chatAttachments/" + message.getId() + "/" + originalFileName;

                            ChatAttachment attachment = ChatAttachment.builder()
                                    .message(message)
                                    .fileName(originalFileName)
                                    .fileUrl(fileUrl)
                                    .fileType(file.getContentType())
                                    .build();

                            chatAttachmentRepository.save(attachment);
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

        ChatMember chatMember = chat.getChatMembers().stream()
                .filter(m -> m.getAccount().getId().equals(account.getId())).findFirst().orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 채팅방에 권한이 없습니다."));
        chatMember.setLastActiveAt(LocalDateTime.now());
        chatMemberRepository.save(chatMember);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CreateMessageResponse.from(message));
    }

    @GetMapping // 내가 소속된 채팅방 리스트 가져오기
    public ResponseEntity<?> getMyChats(@RequestAttribute Account account) {
        List<Chat> myChats = chatRepository.findAllByMemberAccountId(account.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ChatListResponse.from(myChats, account));
    }

    @GetMapping("/{chatId}") // 채팅방 상세보기
    public ResponseEntity<?> getChat(@RequestAttribute Account account,
                                     @PathVariable String chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
        if (!chat.getChatMembers().stream().map(m -> m.getAccount().getId()).toList().contains(account.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 채팅방에 권한이 없습니다.");
        }

        ChatMember chatMember = chat.getChatMembers().stream()
                .filter(m -> m.getAccount().getId().equals(account.getId())).findFirst().orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 채팅방에 권한이 없습니다."));
        chatMember.setLastActiveAt(LocalDateTime.now());
        chatMemberRepository.save(chatMember);
        return ResponseEntity.status(HttpStatus.OK).body(ChatDetailResponse.from(chat, account, chatMember));
    }


}
