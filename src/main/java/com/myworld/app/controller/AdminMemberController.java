
package com.myworld.app.controller;

import com.myworld.app.entity.Department;
import com.myworld.app.entity.Member;
import com.myworld.app.repository.DepartmentRepository;
import com.myworld.app.service.AdminMemberService;

import jakarta.servlet.http.HttpSession;

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

@Controller
@RequestMapping("/admin/members")
public class AdminMemberController {

    private static final String CSRF_SESSION_KEY =
            "adminMembersCsrfToken";

    private final AdminMemberService adminMemberService;
    private final DepartmentRepository departmentRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AdminMemberController(
            AdminMemberService adminMemberService,
            DepartmentRepository departmentRepository
    ) {
        this.adminMemberService = adminMemberService;
        this.departmentRepository = departmentRepository;
    }

    // =========================================
    // 관리자 회원 관리 페이지
    // =========================================

    @GetMapping
    public String membersPage(
            HttpSession session,
            Model model
    ) {

        Long adminId = getAdminId(session);

        List<Member> pendingMembers =
                adminMemberService.getPendingMembers(adminId);

        List<Member> allMembers =
                adminMemberService.getAllMembers(adminId);

        List<Department> departments =
                departmentRepository
                        .findByActiveTrueOrderByNameAsc();

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
                "csrfToken",
                getOrCreateCsrfToken(session)
        );

        return "admin-members";
    }

    // =========================================
    // 회원 가입 승인
    // =========================================

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

    // =========================================
    // 관리자 세션 및 권한 확인
    // =========================================

    private Long getAdminId(HttpSession session) {

        Object memberId =
                session.getAttribute("loginMemberId");

        if (!(memberId instanceof Long)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        Long adminId = (Long) memberId;

        try {
            adminMemberService.requireAdmin(adminId);
        } catch (SecurityException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "관리자 권한이 필요합니다."
            );
        }

        return adminId;
    }

    // =========================================
    // 관리자 페이지 CSRF 토큰 생성
    // =========================================

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

    // =========================================
    // 관리자 작업 요청 검증
    // =========================================

    private void validateCsrfToken(
            HttpSession session,
            String submittedToken
    ) {

        Object expected =
                session.getAttribute(CSRF_SESSION_KEY);

        if (!(expected instanceof String expectedToken)
                || submittedToken == null) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "유효하지 않은 요청입니다."
            );
        }

        byte[] expectedBytes =
                expectedToken.getBytes(
                        StandardCharsets.UTF_8
                );

        byte[] submittedBytes =
                submittedToken.getBytes(
                        StandardCharsets.UTF_8
                );

        if (!MessageDigest.isEqual(
                expectedBytes,
                submittedBytes
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "유효하지 않은 요청입니다."
            );
        }
    }
}
