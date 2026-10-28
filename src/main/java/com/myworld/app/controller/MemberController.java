
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

    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupForm", new MemberSignupDTO());
        return "signup";
    }


    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(
            @ModelAttribute("signupForm") MemberSignupDTO dto,
            Model model
    ) {
        try {
            // 회원가입 정보 검증 및 DB 저장
            memberService.signup(dto);

            // 회원가입 성공 시 로그인 화면으로 이동
            return "redirect:/login?signupSuccess=true";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());

        } catch (DataIntegrityViolationException e) {

            model.addAttribute(
                    "error",
                    "아이디 또는 닉네임이 이미 사용 중입니다."
            );
        }

        // 회원가입 실패 시 입력했던 정보 유지
        // 비밀번호는 보안상 다시 입력하도록 처리
        MemberSignupDTO safeForm = new MemberSignupDTO();

        safeForm.setUsername(dto.getUsername());
        safeForm.setNickname(dto.getNickname());
        safeForm.setRegion(dto.getRegion());

        model.addAttribute("signupForm", safeForm);

        return "signup";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage(
            Model model,
            HttpSession session
    ) {
        if (session.getAttribute("loginMemberId") != null) {
            return "redirect:/board";
        }

        model.addAttribute("loginForm", new MemberLoginDTO());
        return "login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(
            @ModelAttribute("loginForm") MemberLoginDTO dto,
            HttpServletRequest request,
            Model model
    ) {
        try {
            // MemberService에서 BCrypt matches() 검사
            Member member = memberService.login(dto);

            // 로그인 성공 시 기존 세션 ID 교체
            request.changeSessionId();

            HttpSession session = request.getSession();

            // 비밀번호 대신 회원 ID만 저장
            session.setAttribute("loginMemberId", member.getId());
            session.setAttribute("loginNickname", member.getNickname());

            // 세션 만료: 30분
            session.setMaxInactiveInterval(30 * 60);

            return "redirect:/workspace";

        } catch (IllegalArgumentException e) {
            model.addAttribute(
                    "error",
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );

            MemberLoginDTO safeForm = new MemberLoginDTO();
            safeForm.setUsername(dto.getUsername());

            model.addAttribute("loginForm", safeForm);
            return "login";
        }
    }

    // 로그아웃 처리
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return "redirect:/login?logout";
    }
}
