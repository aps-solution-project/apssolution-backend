package org.example.apssolution.controller;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.dto.request.CreateAccountRequest;
import org.example.apssolution.repository.AccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    final AccountRepository accountRepository;

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest createAccountRequest) {
        Account account = Account.builder()
                .id(createAccountRequest.getAccountId())
                .pw(createAccountRequest.getPw())
                .name(createAccountRequest.getName())
                .email(createAccountRequest.getEmail())
                .role(createAccountRequest.getRole()).build();
        accountRepository.save(account);
        return ResponseEntity.ok("사원 등록 완료");
    }
}
