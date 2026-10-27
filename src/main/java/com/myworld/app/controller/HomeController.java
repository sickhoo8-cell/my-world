
package com.myworld.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("title", "My World");
        model.addAttribute("message", "나의 첫 번째 Spring Boot 웹사이트!");

        return "home";
    }
}
