
package com.myworld.app.controller;

import com.myworld.app.service.MbtiService;
import com.myworld.app.service.CompatibilityService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CompatibilityController {

    private final MbtiService mbtiService;
    private final CompatibilityService compatibilityService;

    public CompatibilityController(
            MbtiService mbtiService,
            CompatibilityService compatibilityService) {

        this.mbtiService = mbtiService;
        this.compatibilityService = compatibilityService;
    }

    @GetMapping("/compatibility")
    public String compatibilityPage() {
        return "compatibility";
    }

    @GetMapping("/compatibility/result")
    public String result(
            @RequestParam String myMbti,
            @RequestParam String partnerMbti,
            Model model) {

        var myInfo = mbtiService.getMbtiInfo(myMbti);
        var partnerInfo = mbtiService.getMbtiInfo(partnerMbti);

        if (myInfo == null || partnerInfo == null) {
            return "redirect:/compatibility";
        }

        model.addAttribute("myMbti", myInfo.type());
        model.addAttribute("partnerMbti", partnerInfo.type());

        model.addAttribute("scores",
                compatibilityService.calculate(
                        myInfo.type(),
                        partnerInfo.type()
                ));

        return "compatibility-result";
    }
}