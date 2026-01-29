package org.example.apssolution.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Chat;
import org.example.apssolution.dto.request.chat.CreateGroupChatRequest;
import org.example.apssolution.dto.response.chat.ChatDirectResponse;
import org.example.apssolution.repository.AccountRepository;
import org.example.apssolution.repository.ChatAttachmentRepository;
import org.example.apssolution.repository.ChatMemberRepository;
import org.example.apssolution.repository.ChatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/chats")
public class ChatController {
    final AccountRepository accountRepository;
    final ChatRepository chatRepository;
    final ChatMemberRepository chatMemberRepository;
    final ChatMemberRepository chatMemberMemberRepository;
    final ChatAttachmentRepository chatAttachmentRepository;


    @PostMapping
    public ResponseEntity<?> createGroupChat(@RequestAttribute Account account,
                                             @RequestBody @Valid CreateGroupChatRequest cgr,
                                             BindingResult bindingResult) {
        List<Account> chatMembers = accountRepository.findAllById(cgr.getMembers());
        chatMembers.add(account);
        String signature = String.join(":", chatMembers.stream()
                .sorted(Comparator.comparing(Account::getId).thenComparing(Account::getRole))
                .map(Account::getId).toList());

        Chat targetChat = chatRepository.findBySignature(signature);
        // 수정중

        return null;
    }

    @PostMapping("/direct/{targetId}") // 1:1 채팅 시작
    public ResponseEntity<?> directChat(@PathVariable("targetId") String targetId,
                                        @RequestAttribute Account account) {
        String signature = String.join(":", Stream.of(targetId, account.getId()).sorted().toList());

        Chat targetChat = chatRepository.findBySignature(signature);

        if(targetChat != null) {
            return ResponseEntity.status(HttpStatus.OK).body(ChatDirectResponse.from(targetChat));
        }

        Chat chat = Chat.builder()
                .owner(account)
                .signature(signature)
                .build();
        chatRepository.save(chat);

        return ResponseEntity.status(HttpStatus.CREATED).body(ChatDirectResponse.from(chat));
    }


}
