package com.portfolio.interview.api;

import com.portfolio.interview.api.dto.SettingsDto;
import com.portfolio.interview.domain.user.UserEntity;
import com.portfolio.interview.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final SettingsService service;

    public SettingsController(SettingsService service) {
        this.service = service;
    }

    @PostMapping
    public Object save(@AuthenticationPrincipal UserEntity user,
                       @RequestBody @Valid SettingsDto.SaveReq req) {
        var s = service.save(user, req.role, req.difficulty, req.language);
        return new Object() {
            public Long id = s.id;
            public String role = s.role.name();
            public String difficulty = s.difficulty.name();
            public String language = s.language.name();
        };
    }

    @GetMapping("/me")
    public SettingsDto.Res me(@AuthenticationPrincipal UserEntity user) {
        var s = service.getLatestOrCreate(user);
        return new SettingsDto.Res(s.role, s.difficulty, s.language);
    }
}