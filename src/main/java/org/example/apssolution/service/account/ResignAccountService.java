package org.example.apssolution.service.account;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.dto.response.service.ServiceResultResponse;
import org.example.apssolution.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ResignAccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public ServiceResultResponse resign(String accountId) {

        Account account = accountRepository.findById(accountId)
                .orElse(null);

        if (account == null) {
            return ServiceResultResponse.builder().success(false).message("존재하지 않는 사원입니다.").build();
        }

        if (account.getResignedAt() != null) {
            return ServiceResultResponse.builder().success(false).message("이미 퇴직 처리된 사원입니다.").build();
        }

        account.setResignedAt(LocalDate.now());

        return ServiceResultResponse.builder().success(true).message("퇴직 처리 완료").build();
    }
}
