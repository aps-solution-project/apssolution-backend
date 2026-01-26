package org.example.apssolution.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.enums.Role;
import org.example.apssolution.dto.request.account.*;
import org.example.apssolution.dto.response.account.*;
import org.example.apssolution.dto.response.service.ServiceResultResponse;
import org.example.apssolution.repository.AccountRepository;
import org.example.apssolution.service.account.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final CreateAccountService createAccountService;
    private final JwtProviderService jwtProviderService;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EditAccountAdminService editAccountAdminService;
    //private final EditAccountService editAccountService;
    private final EditAccountPasswordService editAccountPasswordService;
    private final ResignAccountService resignAccountService;

    private final GetAccountService getAccountService;

    @PostMapping    // 사원 등록
    @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest request,
                                           @RequestAttribute("role") String role) {
        if (request.getPw() == null){
            request.setPw(UUID.randomUUID().toString().split("-")[0]);
        }
        Account account = createAccountService.createAccount(request);
        CreateAccountResponse response = CreateAccountResponse.builder().success(true)
                .message("사원등록 완료(메일 전송 완료)")
                .accountId(account.getId()).accountName(request.getName())
                .accountEmail(request.getEmail()).role(request.getRole()).build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/login")  // 로그인
    public ResponseEntity<?> postLogin(@RequestBody LoginAccountRequest request) {
        Account account = accountRepository.findById(request.getAccountId()).orElse(null);

        if (account == null) {
            return ResponseEntity.badRequest().body("존재하지 않은 사원번호 입니다.");
        }
        if (!passwordEncoder.matches(request.getPw(), account.getPw())) {
            return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
        }
        if (account.getResignedAt() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("퇴사 처리된 계정입니다");
        }

        String token = jwtProviderService.createToken(account);
        LoginResponse response = LoginResponse.builder()
                .success(true)
                .message("로그인 성공")
                .accountId(account.getId())
                .accountName(account.getName())
                .token(token)
                .role(account.getRole())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{accountId}") // 관리자 사원 정보 수정
    @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<?> editAccountAdmin(@PathVariable String accountId,
                                              @RequestBody EditAccountAdminRequest request,
                                              @RequestAttribute("role") String role) {
        if (Role.ADMIN.name().equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("ADMIN 권한이 필요합니다");
        }

        ServiceResultResponse result = editAccountAdminService.editAccountAdmin(accountId, role, request);
        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getMessage());
        }

        return ResponseEntity.ok("사원 정보 수정 완료");
    }

    @GetMapping // 전체 사원 조회
    @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<?> getAccounts() {
        List<Account> allAccount = accountRepository.findAll();
        List<GetAccountDTO> accountDTOS = allAccount.stream().map(e -> GetAccountDTO.builder()
                .accountId(e.getId()).accountName(e.getName()).accountEmail(e.getEmail())
                .profileImageUrl(e.getProfileImageUrl()).role(e.getRole()).workedAt(e.getWorkedAt())
                .resignedAt(e.getResignedAt()).build()).toList();

        GetAccountAllResponse response = GetAccountAllResponse.builder()
                .success(true).message("전체 사원 조회 완료").accounts(accountDTOS).build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{accountId}") // 사원 상세 조회
    @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<?> getAccount(@PathVariable String accountId) {
        GetAccountResponse response = getAccountService.getAccount(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

//    @PatchMapping("/{accountId}/edit")    // 사원 본인 정보 수정
//    public ResponseEntity<?> editAccount(@PathVariable String accountId, @RequestBody EditAccountRequest request,
//                                         @RequestAttribute("tokenId") String tokenId) {
//        if (!tokenId.equals(accountId)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인의 정보만 수정할 수 있습니다.");
//        }
//
//        ServiceResultResponse result = editAccountService.editAccount(tokenId, request);
//
//        if (result.isSuccess()) {
//            EditAccountResponse response = EditAccountResponse.builder().success(true)
//                    .message(result.getMessage()).build();
//            return ResponseEntity.status(HttpStatus.OK).body(response);
//        } else {
//            EditAccountResponse response = EditAccountResponse.builder().success(false)
//                    .message(result.getMessage()).build();
//            if ("존재하지 않는 계정입니다.".equals(result.getMessage())) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getMessage());
//            } else if ("퇴사한 계정은 수정할 수 없습니다.".equals(result.getMessage())) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getMessage());
//            } else {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.getMessage());
//            }
//        }
//    }

    @PatchMapping("/{accountId}/edit") // 본인 프로필 수정
    @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<?> editAccount(@PathVariable String accountId,
                                         @ModelAttribute EditAccountRequest request,
                                         @RequestAttribute Account account) throws IOException {
        Path uplodadPath = Path.of(System.getProperty("user.home"), "apssolution", "profile", accountId);
        Files.createDirectories(uplodadPath);

        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        Path filePath = uplodadPath.resolve(uuid);
        request.getProfileImage().transferTo(filePath.toFile());

        String imageUri = "/apssolution/profile/" + accountId + "/" + uuid;

        account.setProfileImageUrl(imageUri);
        accountRepository.save(account);

        return ResponseEntity.status(HttpStatus.OK)
                .body(EditAccountResponse.builder()
                        .success(true)
                        .message("Account Edit Complete")
                        .build());
    }

    @PatchMapping("/{accountId}/password")    // 비밀번호 변경
    @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<?> editPassword(@PathVariable String accountId,
                                          @RequestBody EditAccountPasswordRequest request,
                                          @RequestAttribute("tokenId") String tokenId,
                                          @RequestAttribute("role") String role) {
        if (!tokenId.equals(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인의 비밀번호만 수정할 수 있습니다.");
        }

        ServiceResultResponse result = editAccountPasswordService.editPw(accountId, request);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getMessage());
        } else {
            if ("존재하지 않는 사원입니다.".equals(result.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.getMessage());
            }
        }
    }

    @DeleteMapping("/{accountId}/resign") // 사원 퇴직 처리
    @SecurityRequirement(name="bearerAuth")
    public ResponseEntity<?> resignAccount(@PathVariable String accountId, @RequestAttribute("role") String role) {
        if (!Role.ADMIN.name().equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("ADMIN 권한이 필요합니다.");
        }

        ServiceResultResponse result = resignAccountService.resign(accountId);

        if (!result.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessage());
        }

        ResignAccountResponse response = ResignAccountResponse.builder().success(true).message("퇴직 처리 완료").build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
