package com.portfolio.interview.repo;

import com.portfolio.interview.domain.interview.InterviewQuestionEntity;
import com.portfolio.interview.domain.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<InterviewQuestionEntity, Long> {
    @Query("""
        select q from InterviewQuestionEntity q
        where q.role = :role and q.difficulty = :difficulty
        order by function('rand')
        """)
    Optional<InterviewQuestionEntity> findRandom(Role role, Difficulty difficulty);
}
