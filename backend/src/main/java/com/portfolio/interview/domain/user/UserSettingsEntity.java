package com.portfolio.interview.domain.user;

import com.portfolio.interview.domain.enums.*;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_settings")
public class UserSettingsEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public UserEntity user;

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
