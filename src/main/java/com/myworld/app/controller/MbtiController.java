
package com.myworld.app.controller;

import com.myworld.app.dto.MbtiInfo;
import com.myworld.app.service.MbtiService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MbtiController {

    private final MbtiService mbtiService;

    public MbtiController(MbtiService mbtiService) {
        this.mbtiService = mbtiService;
    }

    @GetMapping("/mbti")
    public String mbtiPage() {
        return "mbti";
    }

    @GetMapping("/mbti/{type}")
    public String mbtiDetail(
            @PathVariable String type,
            Model model) {

        MbtiInfo info = mbtiService.getMbtiInfo(type);

        if (info == null) {
            return "redirect:/mbti";
        }

        model.addAttribute("mbti", info.type());
        model.addAttribute("nickname", info.nickname());
        model.addAttribute("personality", info.personality());
        model.addAttribute("strength", info.strength());
        model.addAttribute("weakness", info.weakness());
        model.addAttribute("loveStyle", info.loveStyle());

        return "mbti-detail";
    }
}
