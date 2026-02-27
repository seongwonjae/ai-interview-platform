package com.portfolio.interview.domain.interview;

import com.portfolio.interview.domain.enums.*;
import com.portfolio.interview.domain.user.UserEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "interview_sessions")
public class InterviewSessionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public UserEntity user;

    @Column(nullable = false)
    public String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Language language;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();
}
