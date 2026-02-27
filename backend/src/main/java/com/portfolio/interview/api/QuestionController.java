package com.portfolio.interview.api;

import com.portfolio.interview.api.dto.QuestionDto;
import com.portfolio.interview.domain.user.UserEntity;
import com.portfolio.interview.service.QuestionService;
import com.portfolio.interview.service.SettingsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final SettingsService settingsService;
    private final QuestionService questionService;

    public QuestionController(SettingsService settingsService, QuestionService questionService) {
        this.settingsService = settingsService;
        this.questionService = questionService;
    }

    @GetMapping("/recommend")
    public QuestionDto.Res recommend(@AuthenticationPrincipal UserEntity user) {
        var s = settingsService.getLatest(user.id);
        var q = questionService.recommend(s.role, s.difficulty);
        return new QuestionDto.Res(q.id, q.text, q.role, q.difficulty, q.category);
    }
}
