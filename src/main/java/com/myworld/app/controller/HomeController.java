
package com.myworld.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 사이트 첫 접속 -> 로그인 화면
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    // 기존 겨울 마이월드 홈페이지
    @GetMapping("/home")
    public String home() {
        return "home";
    }
}
