package com.portfolio.interview.api.dto;

import com.portfolio.interview.domain.enums.*;
public class QuestionDto {
    public static class Res {
        public Long id;
        public String text;
        public Role role;
        public Difficulty difficulty;
        public String category;

        public Res(Long id, String text, Role role, Difficulty difficulty, String category) {
            this.id = id; this.text = text; this.role = role; this.difficulty = difficulty; this.category = category;
        }
    }
}
