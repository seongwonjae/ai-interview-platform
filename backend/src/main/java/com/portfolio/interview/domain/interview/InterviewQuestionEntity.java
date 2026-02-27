package com.portfolio.interview.domain.interview;

import com.portfolio.interview.domain.enums.*;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "interview_questions")
public class InterviewQuestionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Difficulty difficulty;

    @Column(nullable = false)
    public String category;

    @Lob
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    public String text;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();
}
