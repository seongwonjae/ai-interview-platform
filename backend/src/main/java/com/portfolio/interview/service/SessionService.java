package com.portfolio.interview.service;

import com.portfolio.interview.domain.interview.InterviewSessionEntity;
import com.portfolio.interview.domain.user.UserEntity;
import com.portfolio.interview.repo.EvaluationRepository;
import com.portfolio.interview.repo.SessionRepository;
import com.portfolio.interview.repo.SubmissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SessionService {

    private final SessionRepository repo;                 // 세션 repo (기존 그대로)
    private final SettingsService settingsService;         // 기존 그대로
    private final SubmissionRepository submissionRepo;     // ✅ 추가
    private final EvaluationRepository evalRepo;           // ✅ 추가

    public SessionService(
            SessionRepository repo,
            SettingsService settingsService,
            SubmissionRepository submissionRepo,
            EvaluationRepository evalRepo
    ) {
        this.repo = repo;
        this.settingsService = settingsService;
        this.submissionRepo = submissionRepo;
        this.evalRepo = evalRepo;
    }

    public InterviewSessionEntity create(UserEntity user, String title) {
        var settings = settingsService.getLatest(user.id);

        InterviewSessionEntity s = new InterviewSessionEntity();
        s.user = user;
        s.title = title;
        s.role = settings.role;
        s.difficulty = settings.difficulty;
        s.language = settings.language;

        return repo.save(s);
    }

    @Transactional
    public void deleteSession(UserEntity user, Long sessionId) {

        // ✅ 권한 체크 포함 (내 세션만 삭제)
        var session = repo.findByIdAndUser_Id(sessionId, user.id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        // 1) evaluations 먼저 삭제
        evalRepo.deleteBySubmission_Session_Id(sessionId);

        // 2) submissions 삭제
        submissionRepo.deleteBySession_Id(sessionId);

        // 3) session 삭제
        repo.delete(session);
    }
}
