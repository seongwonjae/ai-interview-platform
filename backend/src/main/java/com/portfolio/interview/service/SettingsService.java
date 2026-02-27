package com.portfolio.interview.service;

import com.portfolio.interview.domain.enums.*;
import com.portfolio.interview.domain.user.*;
import com.portfolio.interview.repo.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {
    private final UserSettingsRepository repo;

    public SettingsService(UserSettingsRepository repo) {
        this.repo = repo;
    }

    public UserSettingsEntity save(UserEntity user, Role role, Difficulty difficulty, Language language) {
        UserSettingsEntity s = new UserSettingsEntity();
        s.user = user;
        s.role = role;
        s.difficulty = difficulty;
        s.language = language;
        return repo.save(s);
    }

    public UserSettingsEntity getLatest(Long userId) {
        return repo.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("No settings"));
    }

    @Transactional
    public UserSettingsEntity getLatestOrCreate(UserEntity user) {
        return repo.findFirstByUserIdOrderByCreatedAtDesc(user.id)
            .orElseGet(() -> save(
                user,
                Role.BACKEND,        // ⚠️ enum 실제 값으로 바꿔야 함
                Difficulty.MEDIUM,   // ⚠️
                Language.JAVA        // ⚠️
            ));
    }
}
