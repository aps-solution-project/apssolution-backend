package org.example.apssolution.dto.request.personal_schedule;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.PersonalSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class UpsertScheduleRequest {
    private Long id;
    private String title;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String color;
    private String shift;
    private String description;

    public PersonalSchedule toPersonalSchedule(Account account) {
        return PersonalSchedule.builder()
                .id(id)
                .account(account)
                .title(this.title == null ? "제목 없음" : this.title)
                .date(this.date ==  null ? LocalDate.now() : this.date)
                .startTime(this.startTime == null ? LocalTime.of(0, 0) : this.startTime)
                .endTime(this.endTime == null ? LocalTime.now() : this.endTime)
                .location(this.location == null ? "-" : this.location)
                .color(this.color == null ? "blue" : this.color)
                .shift(this.shift  == null ? "day" : this.shift)
                .description(this.description == null ? "" : this.description)
                .build();
    }
}
