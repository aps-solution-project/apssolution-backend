package org.example.apssolution.service.account;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.dto.request.account.CreateAccountRequest;
import org.example.apssolution.repository.AccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CreateAccountService {

    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;

    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        String accountId = generateAccountId();

        if (accountRepository.existsById(accountId)) {
            throw new IllegalStateException("사원번호 중복");
        }

        Account account = Account.builder()
                .id(accountId)
                .name(request.getName())
                .email(request.getEmail())
                .role(request.getRole())
                .profileImageUrl(request.getProfileImageUrl())
                .pw(passwordEncoder.encode(request.getPw()))
                .build();

        accountRepository.save(account);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(request.getEmail());
        mailMessage.setSubject("사원등록 완료");
        mailMessage.setText("\n사원번호 : " +  account.getId() +
                "\n초기 비밀번호 : " + request.getPw() +
                "입니다.\n\n로그인 후 반드시 비밀번호 변경바랍니다.");
        javaMailSender.send(mailMessage);

        return account;

    }

    private String generateAccountId() {
        String year = String.valueOf(LocalDate.now().getYear()).substring(2);

        String accountId;
        do {
            int rand = ThreadLocalRandom.current().nextInt(0, 1_000_000);
            String code = String.format("%06d", rand);
            accountId = "P" + year + code;
        } while (accountRepository.existsById(accountId));

        return accountId;
    }
}