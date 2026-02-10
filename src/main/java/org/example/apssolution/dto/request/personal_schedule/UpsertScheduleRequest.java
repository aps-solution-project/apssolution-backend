package org.example.apssolution.dto.request.personal_schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.PersonalSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "개인 일정 생성/수정 요청")
@Getter
@Setter
@Builder
public class UpsertScheduleRequest {

    @Schema(description = "일정 ID (없으면 신규 생성)", example = "12")
    private Long id;

    @Schema(description = "일정 제목", example = "회의")
    private String title;

    @Schema(description = "일정 날짜", example = "2026-02-10")
    private LocalDate date;

    @Schema(description = "시작 시간", example = "09:30")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "10:30")
    private LocalTime endTime;

    @Schema(description = "장소", example = "회의실 A")
    private String location;

    @Schema(description = "색상", example = "blue")
    private String color;

    @Schema(description = "근무 타입", example = "day")
    private String shift;

    @Schema(description = "설명", example = "주간 회의")
    private String description;

    public PersonalSchedule toPersonalSchedule(Account account) {
        return PersonalSchedule.builder()
                .account(account)
                .title(this.title == null ? "제목 없음" : this.title)
                .date(this.date == null ? LocalDate.now() : this.date)
                .startTime(this.startTime == null ? LocalTime.of(0, 0) : this.startTime)
                .endTime(this.endTime == null ? LocalTime.of(23, 59) : this.endTime)
                .location(this.location == null ? "-" : this.location)
                .color(this.color == null ? "blue" : this.color)
                .shift(this.shift == null ? "day" : this.shift)
                .description(this.description == null ? "" : this.description)
                .active(true)
                .build();
    }
}