package com.portfolio.interview.api.dto;

import jakarta.validation.constraints.NotBlank;

public class SessionDto {
    public static class CreateReq {
        @NotBlank public String title;
    }
    public static class CreateRes {
        public Long id;
        public String title;
        public String created_at;

        public CreateRes(Long id, String title, String createdAt) {
            this.id = id; this.title = title; this.created_at = createdAt;
        }
    }

    // ✅ 추가: 세션 목록 응답
    public static class ListRes {
        public Long id;
        public String title;
        public String created_at;

        public String latest_status;   // "PENDING" | "PROCESSING" | "DONE" | "FAILED"
        public Integer overall_score;  // 없으면 null

        public ListRes(Long id, String title, String createdAt,
                       String latestStatus, Integer overallScore) {
            this.id = id;
            this.title = title;
            this.created_at = createdAt;
            this.latest_status = latestStatus;
            this.overall_score = overallScore;
        }
    }
}
