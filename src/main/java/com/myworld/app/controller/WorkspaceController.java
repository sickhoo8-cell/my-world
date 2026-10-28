
package com.myworld.app.controller;

import com.myworld.app.entity.Member;
import com.myworld.app.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WorkspaceController {

    private final MemberRepository memberRepository;

    public WorkspaceController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/workspace")
    public String workspace(
            HttpSession session,
            Model model
    ) {

        // 로그인 여부 확인
        Object loginMemberId =
                session.getAttribute("loginMemberId");

        if (!(loginMemberId instanceof Long memberId)) {
            return "redirect:/login";
        }

        // DB에서 로그인한 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElse(null);

        if (member == null) {
            session.invalidate();
            return "redirect:/login";
        }

        // 회원가입 시 선택한 지역
        String region = member.getRegion();

        // 기존 회원은 서울을 기본값으로 사용
        if (region == null || region.isBlank()) {
            region = "서울특별시";
        }

        model.addAttribute("nickname", member.getNickname());
        model.addAttribute("region", region);

        return "workspace";
    }
}
