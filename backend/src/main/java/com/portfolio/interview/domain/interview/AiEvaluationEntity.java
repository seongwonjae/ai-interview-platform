package com.portfolio.interview.domain.interview;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ai_evaluations")
public class AiEvaluationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    public InterviewSubmissionEntity submission;

    @Column(name = "prompt_version", nullable = false)
    public String promptVersion;

    @Column(name = "overall_score", nullable = false)
    public int overallScore;

    @Column(name = "score_structure", nullable = false)
    public int scoreStructure;

    @Column(name = "score_clarity", nullable = false)
    public int scoreClarity;

    @Column(name = "score_relevance", nullable = false)
    public int scoreRelevance;

    @Column(name = "strengths_json", nullable = false, columnDefinition = "json")
    public String strengthsJson;

    @Column(name = "improvements_json", nullable = false, columnDefinition = "json")
    public String improvementsJson;

    @Lob
    @Column(name = "rewritten_answer", nullable = false, columnDefinition = "TEXT")
    public String rewrittenAnswer;

    @Column(name = "raw_response_json", nullable = false, columnDefinition = "json")
    public String rawResponseJson;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();
}
