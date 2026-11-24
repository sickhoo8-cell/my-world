
package com.myworld.app.controller;

import com.myworld.app.entity.Member;
import com.myworld.app.repository.MemberRepository;
import com.myworld.app.service.MyPageService;

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

@Controller
@RequestMapping("/mypage")
public class MyPageController {

    private static final String CSRF_KEY =
            "myPageCsrfToken";

    private final MemberRepository memberRepository;
    private final MyPageService myPageService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public MyPageController(
            MemberRepository memberRepository,
            MyPageService myPageService
    ) {
        this.memberRepository = memberRepository;
        this.myPageService = myPageService;
    }

    // =========================================
    // 1. 마이페이지 조회
    // =========================================

    @GetMapping
    public String myPage(
            HttpSession session,
            Model model
    ) {

        Long memberId = requireLogin(session);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        if (!member.isApproved()) {
            session.invalidate();
            return "redirect:/login?accountStatus";
        }

        model.addAttribute("member", member);

        model.addAttribute(
                "loginNickname",
                member.getNickname()
        );

        model.addAttribute(
                "csrfToken",
                getOrCreateCsrfToken(session)
        );

        return "mypage";
    }

    // =========================================
    // 2. 닉네임 및 지역 변경
    // =========================================

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam String nickname,
            @RequestParam String region,
            @RequestParam String csrfToken,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        Long memberId = requireLogin(session);

        validateCsrfToken(session, csrfToken);

        try {

            myPageService.updateProfile(
                    memberId,
                    nickname,
                    region
            );

            // 세션에 저장된 닉네임도 최신 값으로 변경
            session.setAttribute(
                    "loginNickname",
                    nickname.trim()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "회원정보가 수정되었습니다."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (SecurityException e) {

            session.invalidate();
            return "redirect:/login?accountStatus";
        }

        return "redirect:/mypage";
    }

    // =========================================
    // 3. 비밀번호 변경
    // =========================================

    @PostMapping("/password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String passwordConfirm,
            @RequestParam String csrfToken,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        Long memberId = requireLogin(session);

        validateCsrfToken(session, csrfToken);

        try {

            myPageService.changePassword(
                    memberId,
                    currentPassword,
                    newPassword,
                    passwordConfirm
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "비밀번호가 변경되었습니다."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (SecurityException e) {

            session.invalidate();
            return "redirect:/login?accountStatus";
        }

        return "redirect:/mypage";
    }

    // =========================================
    // 4. 로그인 확인
    // =========================================

    private Long requireLogin(HttpSession session) {

        Object value =
                session.getAttribute("loginMemberId");

        if (!(value instanceof Long memberId)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        return memberId;
    }

    // =========================================
    // 5. CSRF 토큰 생성
    // =========================================

    private String getOrCreateCsrfToken(
            HttpSession session
    ) {

        Object existing =
                session.getAttribute(CSRF_KEY);

        if (existing instanceof String token
                && !token.isBlank()) {

            return token;
        }

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        session.setAttribute(CSRF_KEY, token);

        return token;
    }

    // =========================================
    // 6. CSRF 토큰 검증
    // =========================================

    private void validateCsrfToken(
            HttpSession session,
            String submittedToken
    ) {

        Object stored =
                session.getAttribute(CSRF_KEY);

        if (!(stored instanceof String expected)
                || submittedToken == null) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "유효하지 않은 요청입니다."
            );
        }

        byte[] expectedBytes =
                expected.getBytes(StandardCharsets.UTF_8);

        byte[] submittedBytes =
                submittedToken.getBytes(StandardCharsets.UTF_8);

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
