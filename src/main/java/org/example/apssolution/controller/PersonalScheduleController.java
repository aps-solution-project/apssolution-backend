package org.example.apssolution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.PersonalSchedule;
import org.example.apssolution.domain.entity.ScenarioWorker;
import org.example.apssolution.dto.request.personal_schedule.UpsertScheduleRequest;
import org.example.apssolution.dto.response.personal_schedule.UpsertScheduleResponse;
import org.example.apssolution.dto.response.personal_schedule.GetMonthlySchedulesResponse;
import org.example.apssolution.repository.PersonalScheduleRepository;
import org.example.apssolution.repository.ScenarioWorkerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/calendars")
public class PersonalScheduleController {
    private final PersonalScheduleRepository personalScheduleRepository;
    private final ScenarioWorkerRepository scenarioWorkerRepository;

    @Operation(
            summary = "개인 일정 생성/수정",
            description = """
                    개인 일정을 생성하거나 수정합니다.
                    
                    - id가 없으면 신규 생성
                    - id가 있으면 해당 일정 수정
                    - date / startTime / endTime은 ISO-8601 규격 사용
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "일정 생성 또는 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpsertScheduleResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<?> upsertSchedule(@RequestAttribute Account account,
                                            @RequestBody UpsertScheduleRequest csr) {
        PersonalSchedule personalSchedule = null;
        if (csr.getId() == null) {
            personalSchedule = csr.toPersonalSchedule(account);
        } else {
            personalSchedule = personalScheduleRepository.findById(csr.getId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "알 수 없는 스케줄 아이디 입니다."));
            personalSchedule.setTitle(csr.getTitle() == null ? personalSchedule.getTitle() : csr.getTitle());
            personalSchedule.setDate(csr.getDate() == null ? personalSchedule.getDate() : csr.getDate());
            personalSchedule.setStartTime(csr.getStartTime() == null ? personalSchedule.getStartTime() : csr.getStartTime());
            personalSchedule.setEndTime(csr.getEndTime() == null ? personalSchedule.getEndTime() : csr.getEndTime());
            personalSchedule.setLocation(csr.getLocation() == null ? personalSchedule.getLocation() : csr.getLocation());
            personalSchedule.setColor(csr.getColor() == null ? personalSchedule.getColor() : csr.getColor());
            personalSchedule.setShift(csr.getShift() == null ? personalSchedule.getShift() : csr.getShift());
            personalSchedule.setDescription(csr.getDescription() == null ? personalSchedule.getDescription() : csr.getDescription());
        }

        personalScheduleRepository.save(personalSchedule);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UpsertScheduleResponse.fromPersonalSchedule(personalSchedule));
    }

    @Operation(
            summary = "월별 개인 일정 조회",
            description = "로그인한 계정의 개인 일정을 월 단위로 조회합니다. month 파라미터가 없으면 현재 월 기준으로 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "월별 일정 조회 성공",
            content = @Content(schema = @Schema(implementation = GetMonthlySchedulesResponse.class))
    )
    @GetMapping
    public ResponseEntity<?> getSchedules(@RequestAttribute Account account,
                                          @Parameter(description = "조회할 월 (1~12), 미입력 시 현재 월", example = "2")
                                          @RequestParam(defaultValue = "0") Integer month) {
        if (month == 0) {
            month = LocalDate.now().getMonthValue();
        }
        List<PersonalSchedule> personalSchedules = personalScheduleRepository.findMonthlySchedules(account, month);
        List<ScenarioWorker> workers = scenarioWorkerRepository.findByWorker_Id(account.getId());

        workers.forEach(w -> {
            w.setIsRead(true);
        });
        scenarioWorkerRepository.saveAll(workers);

        return ResponseEntity.status(HttpStatus.OK)
                .body(GetMonthlySchedulesResponse.fromPersonalSchedules(personalSchedules));
    }


    @Operation(
            summary = "개인 일정 삭제",
            description = "로그인한 계정의 개인 일정을 삭제합니다."
    )
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long scheduleId) {
        personalScheduleRepository.deleteById(scheduleId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
