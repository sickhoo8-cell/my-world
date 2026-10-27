
package com.myworld.app.service;

import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CompatibilityService {

    public Map<String, Integer> calculate(
            String myMbti, String partnerMbti) {

        Map<String, Integer> scores = new LinkedHashMap<>();

        // 두 MBTI의 각 알파벳이 같은지 비교
        boolean energy = myMbti.charAt(0) == partnerMbti.charAt(0);
        boolean sensing = myMbti.charAt(1) == partnerMbti.charAt(1);
        boolean feeling = myMbti.charAt(2) == partnerMbti.charAt(2);
        boolean lifestyle = myMbti.charAt(3) == partnerMbti.charAt(3);

        scores.put("친구", 55
                + (energy ? 15 : 5)
                + (sensing ? 15 : 5));

        scores.put("연애", 55
                + (feeling ? 15 : 5)
                + (lifestyle ? 10 : 15));

        scores.put("결혼", 50
                + (feeling ? 15 : 5)
                + (lifestyle ? 20 : 5));

        scores.put("취미", 55
                + (sensing ? 20 : 5)
                + (energy ? 10 : 5));

        scores.put("대화", 55
                + (energy ? 10 : 5)
                + (feeling ? 15 : 5));

        scores.put("친밀감", 55
                + (feeling ? 15 : 5)
                + (energy ? 10 : 5));

        return scores;
    }
}
