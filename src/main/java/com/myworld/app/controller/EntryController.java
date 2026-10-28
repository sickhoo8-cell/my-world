
package com.myworld.app.controller;

import com.myworld.app.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EntryController {

    private final MemberRepository memberRepository;

    public EntryController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/entry")
    public String entry(HttpSession session) {

        Object loginMemberId =
                session.getAttribute("loginMemberId");

        // 1. 로그인이 안 되어 있으면 로그인 화면으로
        if (!(loginMemberId instanceof Long memberId)) {
            return "redirect:/login";
        }

        // 2. 실제 존재하는 회원인지 확인
        if (!memberRepository.existsById(memberId)) {
            session.invalidate();
            return "redirect:/login";
        }

        // 3. 로그인된 회원이면 그룹웨어로 바로 이동
        return "redirect:/workspace";
    }
}
