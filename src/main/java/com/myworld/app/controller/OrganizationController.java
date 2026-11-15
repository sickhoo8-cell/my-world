
package com.myworld.app.controller;

import com.myworld.app.entity.Member;
import com.myworld.app.repository.MemberRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class OrganizationController {

    private final MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public OrganizationController(
            MemberRepository memberRepository
    ) {
        this.memberRepository = memberRepository;
    }

    // 직원 화면에 전달할 데이터
    public record Employee(
            Long id,
            String nickname,
            String username,
            String department,
            String position
    ) {
    }

    // 부서별 직원 그룹
    public record DepartmentGroup(
            String name,
            List<Employee> employees
    ) {
    }

    @GetMapping("/organization")
    @Transactional(readOnly = true)
    public String organization(
            HttpSession session,
            Model model
    ) {

        // =====================================
        // 1. 로그인 회원 확인
        // =====================================

        Object value =
                session.getAttribute("loginMemberId");

        if (!(value instanceof Long memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        Member loginMember = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "유효하지 않은 계정입니다."
                        )
                );

        if (!loginMember.isApproved()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "이용할 수 없는 계정입니다."
            );
        }

        // =====================================
        // 2. 승인된 직원만 조회
        // =====================================

        List<Object[]> rows = entityManager
                .createQuery(
                        """
                        SELECT
                            m.id,
                            m.nickname,
                            m.username,
                            d.name,
                            m.position
                        FROM Member m
                        LEFT JOIN m.department d
                        WHERE
                            m.accountStatus IS NULL
                            OR m.accountStatus = 'ACTIVE'
                        ORDER BY
                            d.name ASC,
                            m.nickname ASC
                        """,
                        Object[].class
                )
                .getResultList();

        // =====================================
        // 3. 부서별 그룹 구성
        // =====================================

        Map<String, List<Employee>> grouped =
                new LinkedHashMap<>();

        for (Object[] row : rows) {

            String departmentName =
                    row[3] == null
                            ? "미배정"
                            : (String) row[3];

            String position =
                    row[4] == null
                            || ((String) row[4]).isBlank()
                            ? "직급 미지정"
                            : (String) row[4];

            Employee employee = new Employee(
                    (Long) row[0],
                    (String) row[1],
                    (String) row[2],
                    departmentName,
                    position
            );

            grouped.computeIfAbsent(
                    departmentName,
                    key -> new ArrayList<>()
            ).add(employee);
        }

        // =====================================
        // 4. 화면 데이터 전달
        // =====================================

        List<DepartmentGroup> departmentGroups =
                new ArrayList<>();

        grouped.forEach((name, employees) ->
                departmentGroups.add(
                        new DepartmentGroup(
                                name,
                                employees
                        )
                )
        );

        model.addAttribute(
                "departmentGroups",
                departmentGroups
        );

        model.addAttribute(
                "employeeCount",
                rows.size()
        );

        model.addAttribute(
                "departmentCount",
                departmentGroups.size()
        );

        model.addAttribute(
                "loginMemberId",
                memberId
        );

        return "organization";
    }
}
