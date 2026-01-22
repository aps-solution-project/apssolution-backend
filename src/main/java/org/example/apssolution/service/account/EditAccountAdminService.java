package org.example.apssolution.service.account;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.enums.Role;
import org.example.apssolution.dto.request.account.EditAccountAdminRequest;
import org.example.apssolution.dto.response.service.ServiceResultResponse;
import org.example.apssolution.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EditAccountAdminService {

    private final AccountRepository accountRepository;

    @Transactional
    public ServiceResultResponse editAccountAdmin(String accountId, String role, EditAccountAdminRequest request) {

        if (!"ADMIN".equals(role)) {
            return new ServiceResultResponse(false, "권한이 없습니다.");
        }

        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            return new ServiceResultResponse(false, "존재하지 않는 계정입니다.");
        }
        if (account.getResignedAt() != null) {
            return new ServiceResultResponse(false, "퇴사한 계정은 수정할 수 없습니다.");
        }


        if (request.getName() != null) {
            account.setName(request.getName());
        }
        if (request.getEmail() != null) {
            account.setEmail(request.getEmail());
        }
        if (request.getWorkedAt() != null) {
            account.setWorkedAt(request.getWorkedAt());
        }
        if (request.getRole() != null) {
            account.setRole(request.getRole());
        }

        return new ServiceResultResponse(true, "직원 정보 수정 완료");
    }
}
