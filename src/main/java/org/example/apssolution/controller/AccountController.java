package org.example.apssolution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
@Tag(name = "Account", description = "사원 등록, 조회 및 수정 API")
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
    @Operation(summary = "사원 등록", description = "신규 사원 계정을 생성하는 API. 사원번호는 시스템에서 자동 생성, " +
            "임시 비밀번호는 랜덤 값으로 생성 후 이메일로 발송처리")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사원 등록 성공",
                    content = @Content(schema = @Schema(implementation = CreateAccountResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 사원")
    })
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest request) {

        Account account = createAccountService.createAccount(request);
        CreateAccountResponse response = CreateAccountResponse.builder().success(true)
                .message("사원등록 완료(메일 전송 완료)")
                .accountId(account.getId()).accountName(request.getName())
                .accountEmail(request.getEmail()).role(request.getRole()).build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/login")  // 로그인
    @Operation(summary = "사원 로그인", description = "사원 계정 로그인을 처리하는 API. 사원번호와 비밀번호를 검증한 후 JWT 토큰을 발급. 퇴사 처리된 계정은 로그인할 수 없다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "사원번호 없음"),
            @ApiResponse(responseCode = "409", description = "퇴사 처리된 계정")
    })
    public ResponseEntity<?> postLogin(@RequestBody LoginAccountRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 사원번호입니다."));
        if (!passwordEncoder.matches(request.getPw(), account.getPw())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }
        if (account.getResignedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "퇴사 처리된 계정입니다.");
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
    @Operation(summary = "관리자용 사원 정보 수정", description = "관리자가 특정 사원의 정보를 수정하는 API. 일반 사원은 접근 권한 없음.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사원 정보 수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사원 없음")
    })
    public ResponseEntity<?> editAccountAdmin(@PathVariable String accountId,
                                              @RequestBody EditAccountAdminRequest request,
                                              @RequestAttribute("role") String role) {
        if (!Role.ADMIN.name().equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ADMIN 권한 필요");
        }

        ServiceResultResponse result = editAccountAdminService.editAccountAdmin(accountId, role, request);
        if (!result.isSuccess()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, result.getMessage());
        }

        return ResponseEntity.ok("사원 정보 수정 완료");
    }

    @GetMapping // 전체 사원 조회
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary = "전체 사원 조회", description = "시스템에 등록된 모든 사원 정보 조회. 퇴사 여부를 포함한 사원 목록 반환.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GetAccountAllResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
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
    @Operation(summary = "사원 상세 조회", description = "사원번호 기준으로 단일 사원 상세 정보를 조회.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GetAccountDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "사원 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<?> getAccount(@PathVariable String accountId) {
        GetAccountDetailResponse response = getAccountService.getAccount(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{accountId}/edit") // 본인 프로필 수정
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary = "사원용 정보 수정", description = "로그인한 사원이 본인 프로필 정보를 수정하는 API. 프로필 이미지를 포함한 정보 수정 가능")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = EditAccountResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "본인 계정 아님"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
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
    @Operation(summary = "사원용 비밀번호 변경", description = "로그인한 사원이 본인 비밀번호 변경. 본인 계정이 아닌 경우 접근 불가.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "403", description = "본인 계정 아님"),
            @ApiResponse(responseCode = "404", description = "사원 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<?> editPassword(@PathVariable String accountId,
                                          @RequestBody EditAccountPasswordRequest request,
                                          @RequestAttribute("tokenId") String tokenId,
                                          @RequestAttribute("role") String role) {
        if (!tokenId.equals(accountId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 비밀번호만 수정할 수 있습니다.");
        }


        ServiceResultResponse result = editAccountPasswordService.editPw(accountId, request);
        if (!result.isSuccess()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    result.getMessage()
            );
        }

        return ResponseEntity.ok("비밀번호 변경 완료");
    }

    @DeleteMapping("/{accountId}/resign") // 사원 퇴직 처리
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary = "퇴직 사원 등록", description = "특정 사원을 퇴직 상태로 변경. 관리자 권한 필요.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "퇴직 처리 성공",
                    content = @Content(schema = @Schema(implementation = ResignAccountResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "사원 없음")
    })
    public ResponseEntity<?> resignAccount(@PathVariable String accountId, @RequestAttribute("role") String role) {
        if (!Role.ADMIN.name().equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ADMIN 권한이 필요합니다.");
        }

        ServiceResultResponse result = resignAccountService.resign(accountId);

        if (!result.isSuccess()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, result.getMessage());
        }

        ResignAccountResponse response = ResignAccountResponse.builder().success(true).message("퇴직 처리 완료").build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
