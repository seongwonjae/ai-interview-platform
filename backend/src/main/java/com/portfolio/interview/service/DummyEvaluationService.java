package com.portfolio.interview.service;

import com.portfolio.interview.domain.enums.SubmissionStatus;
import com.portfolio.interview.domain.interview.InterviewSubmissionEntity;
import com.portfolio.interview.repo.SubmissionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Service
@ConditionalOnProperty(
        name = "ai.provider",
        havingValue = "dummy",
        matchIfMissing = true
)
public class DummyEvaluationService {

    private final SubmissionRepository submissionRepo;

    public DummyEvaluationService(SubmissionRepository submissionRepo) {
        this.submissionRepo = submissionRepo;
    }

    @Async
    @Transactional
    public void evaluateAsync(Long submissionId) {
        try {
            // AI 처리하는 척 딜레이
            Thread.sleep(2500);

            InterviewSubmissionEntity sub = submissionRepo.findById(submissionId)
                    .orElseThrow(() -> new IllegalStateException("submission not found: " + submissionId));

            // 아직 processing인 것만 처리 (중복 실행 방지)
            if (sub.status != SubmissionStatus.PROCESSING) return;

            int score = ThreadLocalRandom.current().nextInt(55, 96);

            // ✅ 점수/피드백 저장
            sub.overallScore = score;
            sub.feedback = """
            (더미 평가)
            - 전반적인 방향은 좋습니다.
            - 핵심 키워드 정의 → 근거/예시 → 결론 요약 순으로 구성하면 더 설득력이 올라갑니다.
            - 모호한 표현(‘대충’, ‘아마’)을 줄이고, 한 문장 요약을 마지막에 추가해보세요.
            """;

            // ✅ 성공/실패 상태는 done/failed로
            sub.status = (score >= 70) ? SubmissionStatus.DONE : SubmissionStatus.FAILED;

            // 실패가 아니면 에러 메시지는 비움
            if (sub.status == SubmissionStatus.DONE) {
                sub.errorMessage = null;
            } else {
                sub.errorMessage = "더미 평가 기준 점수 미달(" + score + "/100)";
            }

            sub.updatedAt = Instant.now();
            submissionRepo.save(sub);


        } catch (Exception e) {
            // ✅ error 상태가 없으니 failed로 처리
            submissionRepo.findById(submissionId).ifPresent(sub -> {
                sub.status = SubmissionStatus.FAILED;
                sub.errorMessage = e.getMessage();
                sub.updatedAt = Instant.now();
                submissionRepo.save(sub);
            });
        }
    }
}
