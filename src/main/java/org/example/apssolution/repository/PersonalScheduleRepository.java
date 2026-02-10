package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.PersonalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalScheduleRepository extends JpaRepository<PersonalSchedule, Long> {
}
