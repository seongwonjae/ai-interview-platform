package com.portfolio.interview.api.dto;

import com.portfolio.interview.domain.enums.*;
import jakarta.validation.constraints.NotNull;

public class SettingsDto {
    public static class SaveReq {
        @NotNull public Role role;
        @NotNull public Difficulty difficulty;
        @NotNull public Language language;
    }

    public static class Res {
        public Role role;
        public Difficulty difficulty;
        public Language language;

        public Res(Role role, Difficulty difficulty, Language language) {
            this.role = role; this.difficulty = difficulty; this.language = language;
        }
    }
}
