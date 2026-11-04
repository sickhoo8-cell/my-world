
package com.myworld.app.service;

import com.myworld.app.entity.PersonalSchedule;
import com.myworld.app.repository.PersonalScheduleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PersonalScheduleService {
    private final PersonalScheduleRepository repository;

    public PersonalScheduleService(PersonalScheduleRepository repository) {
        this.repository = repository;
    }

    public List<PersonalSchedule> findInRange(Long memberId, LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)
                || ChronoUnit.DAYS.between(from, to) > 31) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "조회 기간은 최대 32일입니다.");
        }
        return repository.findByMemberIdAndEventDateBetweenOrderByEventDateAscEventTimeAscIdAsc(
                memberId, from, to);
    }

    @Transactional
    public PersonalSchedule create(Long memberId, String title, String description,
                                   LocalDate eventDate, LocalTime eventTime) {
        return repository.save(new PersonalSchedule(memberId, validTitle(title),
                validDescription(description), validDate(eventDate), eventTime));
    }

    @Transactional
    public PersonalSchedule update(Long memberId, Long id, String title, String description,
                                   LocalDate eventDate, LocalTime eventTime) {
        PersonalSchedule schedule = findOwned(memberId, id);
        schedule.update(validTitle(title), validDescription(description),
                validDate(eventDate), eventTime);
        return schedule;
    }

    @Transactional
    public void delete(Long memberId, Long id) {
        repository.delete(findOwned(memberId, id));
    }

    private PersonalSchedule findOwned(Long memberId, Long id) {
        return repository.findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "일정을 찾을 수 없습니다."));
    }

    private String validTitle(String value) {
        if (value == null || value.isBlank() || value.trim().length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "제목을 1~120자로 입력해 주세요.");
        }
        return value.trim();
    }

    private String validDescription(String value) {
        String text = value == null ? "" : value.trim();
        if (text.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "설명은 1000자 이하로 입력해 주세요.");
        }
        return text;
    }

    private LocalDate validDate(LocalDate date) {
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "날짜를 선택해 주세요.");
        }
        return date;
    }
}
