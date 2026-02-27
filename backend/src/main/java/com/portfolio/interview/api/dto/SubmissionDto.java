package com.portfolio.interview.api.dto;

import jakarta.validation.constraints.NotBlank;

public class SubmissionDto {

    public static class SubmitReq {
        @NotBlank public String answer_text;
        public String prompt_version = "v1";
    }

    public static class SubmitRes {
        public Long submission_id;
        public String status;

        public SubmitRes(Long id, String status) {
            this.submission_id = id;
            this.status = status;
        }
    }

    public static class StatusRes {
        public Long submission_id;
        public String status;
        public String error_message;

        // 🔥 추가
        public Integer overall_score;
        public String feedback;

        public StatusRes(Long id,
                         String status,
                         String err,
                         Integer overallScore,
                         String feedback) {
            this.submission_id = id;
            this.status = status;
            this.error_message = err;
            this.overall_score = overallScore;
            this.feedback = feedback;
        }
    }
}
