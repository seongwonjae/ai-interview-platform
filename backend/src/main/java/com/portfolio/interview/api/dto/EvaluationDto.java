package com.portfolio.interview.api.dto;

import java.util.List;

public class EvaluationDto {
    public static class Res {
        public Long submission_id;
        public int overall_score;
        public Scores scores;
        public List<String> strengths;
        public List<String> improvements;
        public String rewritten_answer;
        public String prompt_version;
        public String created_at;

        public Res(Long submissionId, int overall, Scores scores, List<String> strengths,
                   List<String> improvements, String rewritten, String promptVersion, String createdAt) {
            this.submission_id = submissionId;
            this.overall_score = overall;
            this.scores = scores;
            this.strengths = strengths;
            this.improvements = improvements;
            this.rewritten_answer = rewritten;
            this.prompt_version = promptVersion;
            this.created_at = createdAt;
        }
    }

    public static class Scores {
        public int structure;
        public int clarity;
        public int relevance;

        public Scores(int structure, int clarity, int relevance) {
            this.structure = structure; this.clarity = clarity; this.relevance = relevance;
        }
    }
}
