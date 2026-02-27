package com.portfolio.interview.repo;

import com.portfolio.interview.domain.interview.InterviewSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<InterviewSubmissionEntity, Long> {

    // ✅ status/evaluation 권한 체크용 (LAZY 접근 제거)
    Optional<InterviewSubmissionEntity> findByIdAndUser_Id(Long id, Long userId);

    // ✅ 세션의 최신 제출 1개 (권한 포함)
    Optional<InterviewSubmissionEntity> findTopBySession_IdAndSession_User_IdOrderByCreatedAtDesc(Long sessionId, Long userId);
    
    @Query("""
    select s from InterviewSubmissionEntity s
    join fetch s.question q
    where s.id = :id
    """)
    Optional<InterviewSubmissionEntity> findByIdWithQuestion(@Param("id") Long id);

    // ✅ SessionController detail용 (question fetch join + 권한 포함)
    @Query("""
        select sub
        from InterviewSubmissionEntity sub
        join fetch sub.question q
        where sub.session.id = :sessionId
          and sub.session.user.id = :userId
        order by sub.createdAt desc
    """)
    List<InterviewSubmissionEntity> findBySessionForUserWithQuestion(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId
    );

    void deleteBySession_Id(Long sessionId);

}
