
package com.myworld.app.controller;

import com.myworld.app.dto.MemberLoginDTO;
import com.myworld.app.dto.MemberSignupDTO;
import com.myworld.app.entity.Member;
import com.myworld.app.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // =========================================
    // 1. 회원가입 페이지
    // =========================================

    @GetMapping("/signup")
    public String signupPage(Model model) {

        model.addAttribute(
                "signupForm",
                new MemberSignupDTO()
        );

        return "signup";
    }

    // =========================================
    // 2. 회원가입 처리
    // =========================================

    @PostMapping("/signup")
    public String signup(
            @ModelAttribute("signupForm")
            MemberSignupDTO dto,
            Model model
    ) {

        try {

            // 기존 회원가입 서비스 유지
            // 신규 회원은 Member 생성자에서
            // PENDING 상태로 등록
            memberService.signup(dto);

            // 회원가입 후 로그인 페이지 이동
            return "redirect:/login?signupSuccess=true";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (DataIntegrityViolationException e) {

            model.addAttribute(
                    "error",
                    "아이디 또는 닉네임이 이미 사용 중입니다."
            );
        }

        // 실패 시 안전한 입력값만 복원
        MemberSignupDTO safeForm = new MemberSignupDTO();

        safeForm.setUsername(dto.getUsername());
        safeForm.setNickname(dto.getNickname());
        safeForm.setRegion(dto.getRegion());

        model.addAttribute(
                "signupForm",
                safeForm
        );

        return "signup";
    }

    // =========================================
    // 3. 로그인 페이지
    // =========================================

    @GetMapping("/login")
    public String loginForm(
            HttpSession session,
            Model model
    ) {

        // 기존 로그인 세션 유지
        if (session.getAttribute("loginMemberId") != null) {
            return "redirect:/workspace";
        }

        model.addAttribute(
                "loginForm",
                new MemberLoginDTO()
        );

        return "login";
    }

    // =========================================
    // 4. 로그인 처리
    // =========================================

    @PostMapping("/login")
    public String login(
            @ModelAttribute("loginForm")
            MemberLoginDTO dto,
            HttpServletRequest request,
            Model model
    ) {

        Member member;

        try {

            // 기존 BCrypt 비밀번호 검증
            member = memberService.login(dto);

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );

            return loginFailure(dto, model);
        }

        // =====================================
        // 회원 상태 검사
        // =====================================

        if (member.isPending()) {

            model.addAttribute(
                    "error",
                    "관리자 승인 대기 중인 계정입니다. " +
                            "승인 완료 후 로그인할 수 있습니다."
            );

            return loginFailure(dto, model);
        }

        if (member.isSuspended()) {

            model.addAttribute(
                    "error",
                    "이용이 정지된 계정입니다. " +
                            "관리자에게 문의해 주세요."
            );

            return loginFailure(dto, model);
        }

        // PENDING, SUSPENDED가 아닌
        // 알 수 없는 상태값도 차단
        if (!member.isApproved()) {

            model.addAttribute(
                    "error",
                    "현재 로그인할 수 없는 계정입니다."
            );

            return loginFailure(dto, model);
        }

        // =====================================
        // 로그인 성공
        // =====================================

        // 기존 세션 ID 교체
        request.changeSessionId();

        HttpSession session = request.getSession();

        session.setAttribute(
                "loginMemberId",
                member.getId()
        );

        session.setAttribute(
                "loginNickname",
                member.getNickname()
        );

        // 30분
        session.setMaxInactiveInterval(30 * 60);

        return "redirect:/workspace";
    }

    // =========================================
    // 5. 로그인 실패 공통 처리
    // =========================================

    private String loginFailure(
            MemberLoginDTO dto,
            Model model
    ) {

        MemberLoginDTO safeForm = new MemberLoginDTO();

        safeForm.setUsername(dto.getUsername());

        model.addAttribute(
                "loginForm",
                safeForm
        );

        return "login";
    }

    // =========================================
    // 6. 로그아웃
    // =========================================

    @PostMapping("/logout")
    public String logout(
            HttpServletRequest request
    ) {

        HttpSession session =
                request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return "redirect:/login?logout";
    }
}
