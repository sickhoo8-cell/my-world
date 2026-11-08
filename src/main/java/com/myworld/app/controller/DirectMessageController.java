
package com.myworld.app.controller;

import com.myworld.app.service.DirectMessageService;
import com.myworld.app.service.DirectMessageService.MessageView;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class DirectMessageController {

    private static final String CSRF_KEY =
            "messengerCsrfToken";

    private final DirectMessageService messageService;
    private final SecureRandom secureRandom =
            new SecureRandom();

    public DirectMessageController(
            DirectMessageService messageService
    ) {
        this.messageService = messageService;
    }

    // =========================================
    // CSRF 토큰 발급
    // =========================================

    @GetMapping("/csrf")
    public Map<String, String> getCsrfToken(
            HttpSession session
    ) {

        requireLogin(session);

        Object existing =
                session.getAttribute(CSRF_KEY);

        if (existing instanceof String token
                && !token.isBlank()) {

            return Map.of("token", token);
        }

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        session.setAttribute(CSRF_KEY, token);

        return Map.of("token", token);
    }

    // =========================================
    // 대화 조회
    // =========================================

    @GetMapping("/messages")
    public List<MessageView> getMessages(
            @RequestParam Long partnerId,
            HttpSession session
    ) {

        Long memberId = requireLogin(session);

        try {
            return messageService.getConversation(
                    memberId,
                    partnerId
            );

        } catch (SecurityException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "채팅 이용 권한이 없습니다."
            );

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    // =========================================
    // 메시지 전송
    // =========================================

    @PostMapping("/messages")
    public ResponseEntity<MessageView> sendMessage(
            @RequestBody SendMessageRequest request,
            @RequestHeader(
                    value = "X-Chat-CSRF",
                    required = false
            ) String submittedToken,
            HttpSession session
    ) {

        Long memberId = requireLogin(session);

        validateCsrf(session, submittedToken);

        try {

            MessageView saved = messageService.sendMessage(
                    memberId,
                    request.partnerId(),
                    request.content()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(saved);

        } catch (SecurityException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "채팅 이용 권한이 없습니다."
            );

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    // =========================================
    // 로그인 세션 확인
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
    // CSRF 검증
    // =========================================

    private void validateCsrf(
            HttpSession session,
            String submittedToken
    ) {

        Object stored = session.getAttribute(CSRF_KEY);

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

    public record SendMessageRequest(
            Long partnerId,
            String content
    ) {
    }
}
