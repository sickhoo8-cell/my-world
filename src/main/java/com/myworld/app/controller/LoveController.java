
package com.myworld.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoveController {

    @GetMapping("/love")
    public String love() {
        return "love";
    }
}
