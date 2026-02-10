package org.example.apssolution.dto.response.personal_schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.PersonalSchedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "월별 개인 일정 조회 응답")
@Getter
@Setter
@Builder
public class GetMonthlySchedulesResponse {

    @Schema(description = "월별 일정 목록")
    private List<Item> monthlySchedules;

    @Getter
    @Setter
    @Builder
    public static class Item{
        @Schema(description = "일정 ID", example = "10")
        private Long id;

        @Schema(description = "계정 ID", example = "user_123")
        private String accountId;

        @Schema(description = "계정 이름", example = "홍길동")
        private String accountName;

        @Schema(description = "일정 제목", example = "회의")
        private String title;

        @Schema(description = "일정 날짜", example = "2026-02-10")
        private LocalDate date;

        @Schema(description = "시작 시간", example = "09:00")
        private LocalTime startTime;

        @Schema(description = "종료 시간", example = "10:30")
        private LocalTime endTime;

        @Schema(description = "장소", example = "회의실 A")
        private String location;

        @Schema(description = "색상", example = "blue")
        private String color;

        @Schema(description = "근무 타입", example = "day")
        private String shift;

        @Schema(description = "설명", example = "월간 회의")
        private String description;

        public static Item fromPersonalSchedule(PersonalSchedule personalSchedule) {
            return Item.builder()
                    .id(personalSchedule.getId())
                    .accountId(personalSchedule.getAccount().getId())
                    .accountName(personalSchedule.getAccount().getName())
                    .title(personalSchedule.getTitle())
                    .date(personalSchedule.getDate())
                    .startTime(personalSchedule.getStartTime())
                    .endTime(personalSchedule.getEndTime())
                    .location(personalSchedule.getLocation())
                    .color(personalSchedule.getColor())
                    .shift(personalSchedule.getShift())
                    .description(personalSchedule.getDescription())
                    .build();
        }
    }


    public static GetMonthlySchedulesResponse fromPersonalSchedules(List<PersonalSchedule> schedules) {
        return GetMonthlySchedulesResponse.builder()
                .monthlySchedules(schedules.stream()
                        .map(Item::fromPersonalSchedule)
                        .toList())
                .build();
    }
}
