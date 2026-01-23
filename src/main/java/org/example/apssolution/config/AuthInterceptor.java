package org.example.apssolution.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.enums.Role;
import org.example.apssolution.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final AccountRepository accountRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String tokenId = (String) request.getAttribute("tokenId");
        Account account = accountRepository.findById(tokenId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "직원 정보를 찾을 수 없습니다"
                        )
                );

        if (account.getResignedAt() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "퇴사자 계정입니다");
        }

        request.setAttribute("account", account);
        return true;
    }
}
