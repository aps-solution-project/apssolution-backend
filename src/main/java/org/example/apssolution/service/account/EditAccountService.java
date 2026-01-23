//package org.example.apssolution.service.account;
//
//import lombok.RequiredArgsConstructor;
//import org.example.apssolution.domain.entity.Account;
//import org.example.apssolution.dto.request.account.EditAccountRequest;
//import org.example.apssolution.dto.response.service.ServiceResultResponse;
//import org.example.apssolution.repository.AccountRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//public class EditAccountService {
//
//    private final AccountRepository accountRepository;
//
//    @Transactional
//    public ServiceResultResponse editAccount(String authAccountId, EditAccountRequest request) {
//        Account account = accountRepository.findById(authAccountId).orElse(null);
//
//        if (account == null) {
//            return new ServiceResultResponse(false, "존재하지 않는 계정입니다.");
//        }
//        if (account.getResignedAt() != null) {
//            return new ServiceResultResponse(false, "퇴사한 계정은 수정할 수 없습니다.");
//        }
//
//
//        if (request.getEmail() != null) {
//            account.setEmail(request.getEmail());
//        }
//        if (request.getProfileImageUrl() != null) {
//            account.setProfileImageUrl(request.getProfileImageUrl());
//        }
//        return new ServiceResultResponse(true, "정보 수정 완료");
//    }
//}
