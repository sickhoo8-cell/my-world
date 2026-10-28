
package com.myworld.app.config;

import com.myworld.app.entity.Member;
import com.myworld.app.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice(annotations = Controller.class)
public class LoginMemberAdvice {

    private final MemberRepository memberRepository;

    public LoginMemberAdvice(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @ModelAttribute
    public void addLoginMember(
            HttpServletRequest request,
            Model model
    ) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        Object memberId = session.getAttribute("loginMemberId");

        if (!(memberId instanceof Long id)) {
            return;
        }

        Member member = memberRepository.findById(id)
                .orElse(null);

        if (member == null) {
            session.invalidate();
            return;
        }

        model.addAttribute("loginMemberId", member.getId());
        model.addAttribute("loginNickname", member.getNickname());
        model.addAttribute("loginRole", member.getRole());
    }
}
