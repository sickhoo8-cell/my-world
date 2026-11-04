
package com.myworld.app.repository;

import com.myworld.app.entity.PersonalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PersonalScheduleRepository extends JpaRepository<PersonalSchedule, Long> {
    List<PersonalSchedule> findByMemberIdAndEventDateBetweenOrderByEventDateAscEventTimeAscIdAsc(
            Long memberId, LocalDate from, LocalDate to);

    Optional<PersonalSchedule> findByIdAndMemberId(Long id, Long memberId);
}
