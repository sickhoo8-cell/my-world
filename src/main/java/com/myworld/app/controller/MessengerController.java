
package com.myworld.app.controller;

import com.myworld.app.service.MessengerDirectoryService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class MessengerController {

    private final MessengerDirectoryService directoryService;

    public MessengerController(
            MessengerDirectoryService directoryService
    ) {
        this.directoryService = directoryService;
    }

    @GetMapping("/messenger")
    public String messengerPage(
            HttpSession session,
            Model model
    ) {

        Object memberId =
                session.getAttribute("loginMemberId");

        // 로그인되지 않았다면 로그인 페이지로 이동
        if (!(memberId instanceof Long loginMemberId)) {
            return "redirect:/login";
        }

        try {

            model.addAttribute(
                    "employees",
                    directoryService.getEmployees(
                            loginMemberId
                    )
            );

        } catch (SecurityException e) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "메신저 접근 권한이 없습니다."
            );
        }

        Object nickname =
                session.getAttribute("loginNickname");

        model.addAttribute(
                "loginNickname",
                nickname == null
                        ? "사용자"
                        : nickname.toString()
        );

        return "messenger";
    }
}
