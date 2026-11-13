
package com.myworld.app.controller;

import com.myworld.app.entity.Department;
import com.myworld.app.entity.Member;
import com.myworld.app.repository.DepartmentRepository;
import com.myworld.app.service.AdminMemberService;

import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/admin/members")
public class AdminMemberController {

    private static final String CSRF_SESSION_KEY =
            "adminMembersCsrfToken";

    private final AdminMemberService adminMemberService;
    private final DepartmentRepository departmentRepository;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public AdminMemberController(
            AdminMemberService adminMemberService,
            DepartmentRepository departmentRepository
    ) {
        this.adminMemberService = adminMemberService;
        this.departmentRepository = departmentRepository;
    }

    // =====================================
    // 관리자 화면
    // =====================================

    @GetMapping
    public String membersPage(
            HttpSession session,
            Model model
    ) {

        Long adminId = getAdminId(session);

        List<Member> allMembers =
                adminMemberService.getAllMembers(adminId);

        List<Member> pendingMembers =
                allMembers.stream()
                        .filter(Member::isPending)
                        .toList();

        List<Department> departments =
                departmentRepository
                        .findByActiveTrueOrderByNameAsc();

        long activeCount = allMembers.stream()
                .filter(Member::isApproved)
                .count();

        long suspendedCount = allMembers.stream()
                .filter(Member::isSuspended)
                .count();

        model.addAttribute(
                "pendingMembers",
                pendingMembers
        );

        model.addAttribute(
                "allMembers",
                allMembers
        );

        model.addAttribute(
                "departments",
                departments
        );

        model.addAttribute(
                "pendingCount",
                pendingMembers.size()
        );

        model.addAttribute(
                "memberCount",
                allMembers.size()
        );

        model.addAttribute(
                "activeCount",
                activeCount
        );

        model.addAttribute(
                "suspendedCount",
                suspendedCount
        );

        model.addAttribute(
                "loginAdminId",
                adminId
        );

        model.addAttribute(
                "csrfToken",
                getOrCreateCsrfToken(session)
        );

        return "admin-members";
    }

    // =====================================
    // 부서 등록 - 기존 기능
    // =====================================

    @PostMapping("/departments")
    public String createDepartment(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam String csrfToken,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        Long adminId = getAdminId(session);

        validateCsrfToken(session, csrfToken);

        adminMemberService.requireAdmin(adminId);

        try {

            String validCode = code == null
                    ? ""
                    : code.trim()
                    .toUpperCase(Locale.ROOT);

            String validName = name == null
                    ? ""
                    : name.trim();

            if (!validCode.matches("[A-Z0-9_]{2,30}")) {
                throw new IllegalArgumentException(
                        "부서 코드는 영문 대문자, 숫자, " +
                                "밑줄을 사용해 2~30자로 입력해주세요."
                );
            }

            if (validName.isBlank()
                    || validName.length() > 100) {

                throw new IllegalArgumentException(
                        "부서명은 1~100자로 입력해주세요."
                );
            }

            if (departmentRepository.existsByCode(
                    validCode
            )) {

                throw new IllegalArgumentException(
                        "이미 등록된 부서 코드입니다."
                );
            }

            Department department =
                    new Department(
                            validCode,
                            validName
                    );

            departmentRepository.saveAndFlush(
                    department
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "부서가 등록되었습니다."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (DataIntegrityViolationException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "부서 등록에 실패했습니다. " +
                            "중복된 코드가 있는지 확인해주세요."
            );
        }

        return "redirect:/admin/members";
    }

    // =====================================
    // 회원 승인 - 기존 기능
    // =====================================

    @PostMapping("/{memberId}/approve")
    public String approveMember(
            @PathVariable Long memberId,
            @RequestParam Long departmentId,
            @RequestParam String position,
            @RequestParam String csrfToken,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        Long adminId = getAdminId(session);

        validateCsrfToken(session, csrfToken);

        try {

            adminMemberService.approveMember(
                    adminId,
                    memberId,
                    departmentId,
                    position
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "회원 승인이 완료되었습니다."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/members";
    }

    // =====================================
    // 회원 정지 - 기존 기능
    // =====================================

    @PostMapping("/{memberId}/suspend")
    public String suspendMember(
            @PathVariable Long memberId,
            @RequestParam String csrfToken,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        Long adminId = getAdminId(session);

        validateCsrfToken(session, csrfToken);

        try {

            adminMemberService.suspendMember(
                    adminId,
                    memberId
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "회원 계정이 정지되었습니다."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/members";
    }

    // =====================================
    // 회원 정지 해제 - 기존 기능
    // =====================================

    @PostMapping("/{memberId}/resume")
    public String resumeMember(
            @PathVariable Long memberId,
            @RequestParam String csrfToken,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        Long adminId = getAdminId(session);

        validateCsrfToken(session, csrfToken);

        try {

            adminMemberService.resumeMember(
                    adminId,
                    memberId
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "회원 계정 정지가 해제되었습니다."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/members";
    }

    // =====================================
    // 신규: 직원 부서 변경
    // =====================================

    @PostMapping("/{memberId}/department")
    public String changeMemberDepartment(
            @PathVariable Long memberId,
            @RequestParam Long departmentId,
            @RequestParam String csrfToken,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        Long adminId = getAdminId(session);

        validateCsrfToken(session, csrfToken);

        try {

            adminMemberService.changeDepartment(
                    adminId,
                    memberId,
                    departmentId
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "직원의 소속 부서가 변경되었습니다."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/members";
    }

    // =====================================
    // 신규: 직원 직급 변경
    // =====================================

    @PostMapping("/{memberId}/position")
    public String changeMemberPosition(
            @PathVariable Long memberId,
            @RequestParam String position,
            @RequestParam String csrfToken,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        Long adminId = getAdminId(session);

        validateCsrfToken(session, csrfToken);

        try {

            adminMemberService.changePosition(
                    adminId,
                    memberId,
                    position
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "직원의 직급이 변경되었습니다."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/members";
    }

    // =====================================
    // 관리자 인증
    // =====================================

    private Long getAdminId(HttpSession session) {

        Object value =
                session.getAttribute("loginMemberId");

        if (!(value instanceof Long adminId)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        try {

            adminMemberService.requireAdmin(adminId);

        } catch (SecurityException e) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "관리자 권한이 없습니다."
            );
        }

        return adminId;
    }

    // =====================================
    // CSRF 토큰 생성
    // =====================================

    private String getOrCreateCsrfToken(
            HttpSession session
    ) {

        Object existing =
                session.getAttribute(CSRF_SESSION_KEY);

        if (existing instanceof String token
                && !token.isBlank()) {
            return token;
        }

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        session.setAttribute(
                CSRF_SESSION_KEY,
                token
        );

        return token;
    }

    // =====================================
    // CSRF 토큰 검사
    // =====================================

    private void validateCsrfToken(
            HttpSession session,
            String submittedToken
    ) {

        Object value =
                session.getAttribute(CSRF_SESSION_KEY);

        if (!(value instanceof String expected)
                || submittedToken == null) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "유효하지 않은 요청입니다."
            );
        }

        byte[] expectedBytes =
                expected.getBytes(
                        StandardCharsets.UTF_8
                );

        byte[] actualBytes =
                submittedToken.getBytes(
                        StandardCharsets.UTF_8
                );

        if (!MessageDigest.isEqual(
                expectedBytes,
                actualBytes
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "유효하지 않은 요청입니다."
            );
        }
    }
}
