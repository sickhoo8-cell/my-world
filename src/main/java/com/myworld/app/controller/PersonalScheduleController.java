
package com.myworld.app.controller;

import com.myworld.app.entity.PersonalSchedule;
import com.myworld.app.repository.MemberRepository;
import com.myworld.app.service.PersonalScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class PersonalScheduleController {
    private final PersonalScheduleService service;
    private final MemberRepository members;

    public PersonalScheduleController(PersonalScheduleService service, MemberRepository members) {
        this.service = service;
        this.members = members;
    }

    @GetMapping
    public List<ScheduleResponse> list(@RequestParam LocalDate from,
                                       @RequestParam LocalDate to,
                                       HttpServletRequest request) {
        Long memberId = requireMember(request);
        return service.findInRange(memberId, from, to).stream()
                .map(ScheduleResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse create(@RequestBody ScheduleRequest body,
                                   HttpServletRequest request) {
        Long memberId = requireMember(request);
        return ScheduleResponse.from(service.create(memberId, body.title(),
                body.description(), body.eventDate(), body.eventTime()));
    }

    @PutMapping("/{id}")
    public ScheduleResponse update(@PathVariable Long id,
                                   @RequestBody ScheduleRequest body,
                                   HttpServletRequest request) {
        Long memberId = requireMember(request);
        return ScheduleResponse.from(service.update(memberId, id, body.title(),
                body.description(), body.eventDate(), body.eventTime()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, HttpServletRequest request) {
        service.delete(requireMember(request), id);
    }

    private Long requireMember(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("loginMemberId") instanceof Long memberId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (!members.existsById(memberId)) {
            session.invalidate();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "다시 로그인해 주세요.");
        }
        return memberId;
    }

    public record ScheduleRequest(String title, String description,
                                  LocalDate eventDate, LocalTime eventTime) {}

    public record ScheduleResponse(Long id, String title, String description,
                                   LocalDate eventDate, LocalTime eventTime,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static ScheduleResponse from(PersonalSchedule s) {
            return new ScheduleResponse(s.getId(), s.getTitle(), s.getDescription(),
                    s.getEventDate(), s.getEventTime(), s.getCreatedAt(), s.getUpdatedAt());
        }
    }
}
