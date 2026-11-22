
package com.myworld.app.controller;

import com.myworld.app.entity.Member;
import com.myworld.app.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class MyPageController {

    private final MemberRepository memberRepository;

    public MyPageController(
            MemberRepository memberRepository
    ) {
        this.memberRepository = memberRepository;
    }

    // =========================================
    // 마이페이지 조회
    // =========================================

    @GetMapping("/mypage")
    public String myPage(
            HttpSession session,
            Model model
    ) {

        // 로그인 회원 확인
        Object loginMemberId =
                session.getAttribute("loginMemberId");

        if (!(loginMemberId instanceof Long memberId)) {
            return "redirect:/login";
        }

        // 로그인한 회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        // 정지되었거나 승인되지 않은 계정 차단
        if (!member.isApproved()) {

            session.invalidate();

            return "redirect:/login?accountStatus";
        }

        // 회원 정보 전달
        model.addAttribute("member", member);

        model.addAttribute(
                "loginNickname",
                member.getNickname()
        );

        return "mypage";
    }
}
