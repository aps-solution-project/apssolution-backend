package org.example.apssolution.service.account;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.dto.response.account.GetAccountDetailResponse;
import org.example.apssolution.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAccountService {
    private final AccountRepository accountRepository;

    @Transactional
    public GetAccountDetailResponse getAccount(String accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 사원입니다."));

        return GetAccountDetailResponse.from(account);
    }
}
