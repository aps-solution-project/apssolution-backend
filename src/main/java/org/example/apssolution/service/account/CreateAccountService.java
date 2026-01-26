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
import java.util.UUID;
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

        String uuidPw = generatePassword();
        Account account = Account.builder()
                .id(accountId)
                .name(request.getName())
                .email(request.getEmail())
                .role(request.getRole())
                .pw(passwordEncoder.encode(uuidPw))
                .build();

        accountRepository.save(account);

        sendWelcomeMail(account, uuidPw);

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

    private String generatePassword() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);
    }

    private void sendWelcomeMail(Account account, String uuidPw) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("사원등록 완료");
        mailMessage.setText(
                "사원번호 : " + account.getId() +
                        "\n임시 비밀번호 : " + uuidPw +
                        "\n\n로그인 후 반드시 비밀번호를 변경해주세요."
        );
        javaMailSender.send(mailMessage);
    }
}