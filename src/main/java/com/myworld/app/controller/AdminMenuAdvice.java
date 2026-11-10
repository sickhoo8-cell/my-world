
package com.myworld.app.controller;

import com.myworld.app.entity.Member;
import com.myworld.app.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AdminMenuAdvice {

    private final MemberRepository memberRepository;

    public AdminMenuAdvice(
            MemberRepository memberRepository
    ) {
        this.memberRepository = memberRepository;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(HttpSession session) {

        Object loginMemberId =
                session.getAttribute("loginMemberId");

        if (!(loginMemberId instanceof Long memberId)) {
            return false;
        }

        return memberRepository.findById(memberId)
                .map(Member::getRole)
                .map(role -> "ADMIN".equals(role))
                .orElse(false);
    }
}
