package org.example.apssolution.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        Account account = (Account) request.getAttribute("account");
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1. 인증 여부 확인
        if (account == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "인증 정보가 없습니다"
            );
        }

        // 2. 계정 조회는 모두 허용
        //    - GET /api/accounts
        //    - GET /api/accounts/{accountId}
        if ("GET".equals(method) &&
                ("/api/accounts".equals(path) || path.matches("^/api/accounts/[^/]+$"))) {
            return true;
        }

        // 3. PLANNER 또는 ADMIN 접근 가능 영역
        if (path.startsWith("/api/tools")
                || path.startsWith("/api/products")
                || path.startsWith("/api/tasks")
                || path.startsWith("/api/scenarios")) {

            Role role = account.getRole();

            if (role != Role.ADMIN && role != Role.PLANNER) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "ADMIN 혹은 PLANNER 권한이 필요합니다"
                );
            }
            return true;
        }

        // 4. 나머지는 ADMIN 전용
        if (account.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "ADMIN 권한이 필요합니다"
            );
        }

        return true;
    }
}
