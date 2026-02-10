package org.example.apssolution.controller;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.PersonalSchedule;
import org.example.apssolution.dto.request.personal_schedule.UpsertScheduleRequest;
import org.example.apssolution.dto.response.personal_schedule.UpsertScheduleResponse;
import org.example.apssolution.dto.response.personal_schedule.GetMonthlySchedulesResponse;
import org.example.apssolution.repository.PersonalScheduleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/calendars")
public class PersonalScheduleController {
    private final PersonalScheduleRepository personalScheduleRepository;

    @PostMapping
    public ResponseEntity<?> upsertSchedule(@RequestAttribute Account account,
                                          UpsertScheduleRequest csr) {
        PersonalSchedule personalSchedule = csr.toPersonalSchedule(account);
        personalScheduleRepository.save(personalSchedule);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UpsertScheduleResponse.fromPersonalSchedule(personalSchedule));
    }

    @GetMapping
    public ResponseEntity<?> getSchedules(@RequestAttribute Account account,
                                          @RequestParam(defaultValue = "0") Integer month) {
        if(month == 0){
            month = LocalDate.now().getMonthValue();
        }
        List<PersonalSchedule> personalSchedules = personalScheduleRepository.findMonthlySchedules(account, month);

        return ResponseEntity.status(HttpStatus.OK)
                .body(GetMonthlySchedulesResponse.fromPersonalSchedules(personalSchedules));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long scheduleId) {
        personalScheduleRepository.deleteById(scheduleId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
