
package com.myworld.app.config;

import com.myworld.app.entity.Member;
import com.myworld.app.repository.MemberRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;

@Component
public class MemberStatusInterceptor implements HandlerInterceptor {

    private final MemberRepository memberRepository;

    public MemberStatusInterceptor(
            MemberRepository memberRepository
    ) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return denyAccess(
                    request,
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        Object loginMemberId =
                session.getAttribute("loginMemberId");

        if (!(loginMemberId instanceof Long memberId)) {

            session.invalidate();

            return denyAccess(
                    request,
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        Optional<Member> foundMember =
                memberRepository.findById(memberId);

        // DB에서 삭제된 회원은 접근 차단
        if (foundMember.isEmpty()) {

            session.invalidate();

            return denyAccess(
                    request,
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "존재하지 않는 계정입니다."
            );
        }

        Member member = foundMember.get();

        // 승인 대기 또는 정지된 회원 차단
        if (!member.isApproved()) {

            session.invalidate();

            return denyAccess(
                    request,
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "이용할 수 없는 계정입니다."
            );
        }

        // 승인된 회원만 요청 처리
        return true;
    }

    private boolean denyAccess(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        String path = request.getRequestURI();

        String contextPath = request.getContextPath();

        if (contextPath != null
                && !contextPath.isEmpty()
                && path.startsWith(contextPath)) {

            path = path.substring(
                    contextPath.length()
            );
        }

        // API 요청은 HTML 대신 JSON으로 응답
        if (path.equals("/api")
                || path.startsWith("/api/")) {

            response.setStatus(status);

            response.setContentType(
                    "application/json;charset=UTF-8"
            );

            response.setHeader(
                    "Cache-Control",
                    "no-store"
            );

            response.getWriter().write(
                    "{\"success\":false," +
                            "\"message\":\"" + message + "\"}"
            );

            return false;
        }

        // 일반 웹페이지는 로그인 페이지로 이동
        response.sendRedirect(
                request.getContextPath()
                        + "/login?sessionExpired=true"
        );

        return false;
    }
}
