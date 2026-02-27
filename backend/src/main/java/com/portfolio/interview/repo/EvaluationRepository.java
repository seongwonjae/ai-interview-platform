package com.portfolio.interview.repo;

import com.portfolio.interview.domain.interview.AiEvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;



public interface EvaluationRepository extends JpaRepository<AiEvaluationEntity, Long> {

    Optional<AiEvaluationEntity> findBySubmission_Id(Long submissionId);

    Optional<AiEvaluationEntity> findBySubmission_IdAndSubmission_User_Id(Long submissionId, Long userId);

    List<AiEvaluationEntity> findBySubmission_IdIn(List<Long> submissionIds);

    void deleteBySubmission_Session_Id(Long sessionId);

}
