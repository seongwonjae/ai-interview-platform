package com.portfolio.interview.api;

import com.portfolio.interview.api.dto.SessionDto;
import com.portfolio.interview.domain.user.UserEntity;
import com.portfolio.interview.repo.EvaluationRepository;
import com.portfolio.interview.repo.SessionRepository;
import com.portfolio.interview.repo.SubmissionRepository;
import com.portfolio.interview.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final SessionRepository sessionRepo;
    private final SubmissionRepository submissionRepo;
    private final EvaluationRepository evalRepo;

    public SessionController(
            SessionService sessionService,
            SessionRepository sessionRepo,
            SubmissionRepository submissionRepo,
            EvaluationRepository evalRepo
    ) {
        this.sessionService = sessionService;
        this.sessionRepo = sessionRepo;
        this.submissionRepo = submissionRepo;
        this.evalRepo = evalRepo;
    }

    @PostMapping
    public SessionDto.CreateRes create(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody @Valid SessionDto.CreateReq req
    ) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");

        var s = sessionService.create(user, req.title);

        return new SessionDto.CreateRes(
                s.id,
                s.title,
                s.createdAt.toString()
        );
    }

    @GetMapping
    public List<SessionListItem> list(@AuthenticationPrincipal UserEntity user) {
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");

        var sessions = sessionRepo.findByUser_IdOrderByCreatedAtDesc(user.id);

        return sessions.stream().map(s -> {

            // ✅ 세션의 최신 제출 1개 (권한 포함)
            var latestSubOpt = submissionRepo
                     .findTopBySession_IdAndSession_User_IdOrderByCreatedAtDesc(s.id, user.id);

            String latestStatus = "PENDING";
            Integer overallScore = null;

            if (latestSubOpt.isPresent()) {
                var latestSub = latestSubOpt.get();

                // enum이면 name() 사용
                latestStatus = latestSub.status.name().toUpperCase();

                overallScore = latestSub.overallScore;
                if (overallScore == null) {
                overallScore = evalRepo
                        .findBySubmission_Id(latestSub.id)
                        .map(e -> e.overallScore)
                        .orElse(null);
                }

            }

            return new SessionListItem(
                    s.id,
                    s.title,
                    s.createdAt.toString(),
                    latestStatus,
                    overallScore
            );

        }).collect(Collectors.toList());
    }


    @GetMapping("/{sessionId}")
    public SessionDetailRes detail(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long sessionId
    ) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");

        // ✅ 권한 쿼리화: "내 세션" 아니면 여기서 끝 (LAZY 접근 없음)
        var session = sessionRepo.findByIdAndUser_Id(sessionId, user.id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        // ✅ 제출 목록: 내 세션 + 내 제출만 + question을 fetch join으로 같이 로딩
        var subs = submissionRepo.findBySessionForUserWithQuestion(sessionId, user.id);

        // ✅ 성능 개선: 평가 점수 N+1 방지 (submissionIds로 한 번에)
        var submissionIds = subs.stream().map(s -> s.id).collect(Collectors.toList());

        Map<Long, Integer> scoreBySubmissionId = new HashMap<>();
        if (!submissionIds.isEmpty()) {
            var evals = evalRepo.findBySubmission_IdIn(submissionIds);
            for (var e : evals) {
                // submission은 연관이지만, 여기서는 id만 필요하므로 안전하게 처리
                // (만약 여기서도 LAZY가 걱정되면 아래 주석의 대안 사용)
                scoreBySubmissionId.put(e.submission.id, e.overallScore);
            }
        }

        var items = subs.stream()
                .map(sub -> {
                Integer score = sub.overallScore; // ✅ submissions 테이블 점수 우선
                if (score == null) {
                        score = scoreBySubmissionId.getOrDefault(sub.id, null); // fallback
                }

                return new SubmissionItem(
                        sub.id,
                        sub.status.name(),
                        new QuestionItem(sub.question.id, sub.question.text),
                        score
                );
                })
                .collect(Collectors.toList());

        return new SessionDetailRes(
                session.id,
                session.title,
                items
        );
    }

    // ---- response classes (익명 Object 대신: 타입 안정 + 가독성) ----

    @DeleteMapping("/{sessionId}")
        public void delete(
                @AuthenticationPrincipal UserEntity user,
                @PathVariable Long sessionId
        ) {
        if (user == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }

        sessionService.deleteSession(user, sessionId);
    }


    public record SessionListItem(
        Long id,
        String title,
        String createdAt,
        String latestStatus,
        Integer overallScore
        ) {}


    public record SessionDetailRes(Long id, String title, List<SubmissionItem> submissions) {}

    public record SubmissionItem(Long id, String status, QuestionItem question, Integer overall_score) {}

    public record QuestionItem(Long id, String text) {}
}
