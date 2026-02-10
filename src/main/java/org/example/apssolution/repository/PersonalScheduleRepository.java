package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.PersonalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonalScheduleRepository extends JpaRepository<PersonalSchedule, Long> {
    @Query("""
                select ps
                from PersonalSchedule ps
                where ps.account = :account
                  and ps.active = true
                  and month(ps.date) = :month
                order by ps.date asc, ps.startTime asc
            """)
    List<PersonalSchedule> findMonthlySchedules(@Param("account") Account account,
                                                @Param("month") int month);

}
