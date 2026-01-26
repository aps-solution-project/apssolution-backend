package org.example.apssolution.service.account;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.dto.request.account.EditAccountPasswordRequest;
import org.example.apssolution.dto.response.service.ServiceResultResponse;
import org.example.apssolution.repository.AccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EditAccountPasswordService {

    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public ServiceResultResponse editPw(String accountId, EditAccountPasswordRequest request) {
        Account account = accountRepository.findById(accountId).orElse(null);

        if (account == null) {
            return new ServiceResultResponse(false, "존재하지 않는 계정입니다.");
        }
        if (account.getResignedAt() != null) {
            return new ServiceResultResponse(false, "퇴사한 계정은 수정할 수 없습니다.");
        }
        if (passwordEncoder.matches(request.getOldPw(), account.getPw())) {
            return new ServiceResultResponse(false, "기존 비밀번호가 일치하지 않습니다.");
        }
        if (passwordEncoder.matches(request.getNewPw(), account.getPw())) {
            return new ServiceResultResponse(false, "기존 비밀번호와 동일합니다.");
        }
        if (!request.getNewPw().equals(request.getNewPwConfirm())) {
            return new ServiceResultResponse(false, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }


        if (request.getNewPw() != null) {
            account.setPw(passwordEncoder.encode(request.getNewPw()));
        }
        return new ServiceResultResponse(true, "비밀번호가 변경되었습니다.");
    }

//    @Transactional
//    public ServiceResultResponse editPwAdmin(String targetId, EditAccountPasswordAdminRequest request) {
//        Account target = accountRepository.findById(targetId).orElse(null);
//
//        if (target == null) {
//            return new ServiceResultResponse(false, "존재하지 않는 사원입니다.");
//        }
//
//        target.setPw(request.getNewPw());
//
//        return new ServiceResultResponse(true, "사원의 비밀번호가 재설정되었습니다.");
//    }
}
