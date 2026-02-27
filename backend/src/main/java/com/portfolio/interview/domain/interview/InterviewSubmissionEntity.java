package com.portfolio.interview.domain.interview;

import com.portfolio.interview.domain.enums.*;
import com.portfolio.interview.domain.user.UserEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "interview_submissions")
public class InterviewSubmissionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    public InterviewSessionEntity session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    public InterviewQuestionEntity question;

    @Lob
    @Column(name = "answer_text", nullable = false, columnDefinition = "TEXT")
    public String answerText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public SubmissionStatus status = SubmissionStatus.PROCESSING;

    @Column(name = "overall_score")
    public Integer overallScore;

    @Lob
    @Column(name = "feedback", columnDefinition = "TEXT")
    public String feedback;

    
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    public String errorMessage;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt = Instant.now();

    public static InterviewSubmissionEntity newProcessing(
            UserEntity user,
            InterviewSessionEntity session,
            InterviewQuestionEntity question,
            String answerText
    ) {
        InterviewSubmissionEntity sub = new InterviewSubmissionEntity();
        sub.user = user;
        sub.session = session;
        sub.question = question;
        sub.answerText = answerText;
        sub.status = SubmissionStatus.PROCESSING;
        sub.errorMessage = null;
        return sub;
    }
}
