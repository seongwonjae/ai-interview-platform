package com.portfolio.interview.repo;

import com.portfolio.interview.domain.interview.InterviewSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<InterviewSessionEntity, Long> {

    List<InterviewSessionEntity> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<InterviewSessionEntity> findByIdAndUser_Id(Long sessionId, Long userId);

    boolean existsByIdAndUser_Id(Long sessionId, Long userId);
}
