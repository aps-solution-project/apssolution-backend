package org.example.apssolution.dto.response.personal_schedule;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.PersonalSchedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
public class GetMonthlySchedulesResponse {

    private List<Item> MonthlySchedule;

    @Getter
    @Setter
    @Builder
    public static class Item{
        private Long id;
        private String accountId;
        private String accountName;
        private String title;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private String location;
        private String color;
        private String shift;
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
                .MonthlySchedule(schedules.stream()
                        .map(Item::fromPersonalSchedule)
                        .toList())
                .build();
    }
}
