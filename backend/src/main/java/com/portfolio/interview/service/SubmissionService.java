package com.portfolio.interview.service;

import com.portfolio.interview.domain.interview.InterviewQuestionEntity;
import com.portfolio.interview.domain.interview.InterviewSessionEntity;
import com.portfolio.interview.domain.interview.InterviewSubmissionEntity;
import com.portfolio.interview.domain.user.UserEntity;
import com.portfolio.interview.repo.QuestionRepository;
import com.portfolio.interview.repo.SessionRepository;
import com.portfolio.interview.repo.SubmissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SubmissionService {

    private final SessionRepository sessionRepo;
    private final QuestionRepository questionRepo;
    private final SubmissionRepository submissionRepo;

    public SubmissionService(SessionRepository sessionRepo,
                             QuestionRepository questionRepo,
                             SubmissionRepository submissionRepo) {
        this.sessionRepo = sessionRepo;
        this.questionRepo = questionRepo;
        this.submissionRepo = submissionRepo;
    }

    @Transactional
    public InterviewSubmissionEntity submit(
            UserEntity user,
            Long sessionId,
            Long questionId,
            String answerText,
            String promptVersion
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }

        // ✅ 소유권 체크는 한 번만
        if (!sessionRepo.existsByIdAndUser_Id(sessionId, user.id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        InterviewSessionEntity session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        InterviewQuestionEntity question = questionRepo.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        if (answerText == null || answerText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "answer_text is required");
        }

        // ✅ 생성 규칙 고정: PROCESSING으로 생성
        InterviewSubmissionEntity sub =
                InterviewSubmissionEntity.newProcessing(user, session, question, answerText);

        // promptVersion은 현재 InterviewSubmissionEntity에 컬럼이 없으니 저장 못 함.
        // (원하면 컬럼 추가해서 sub.promptVersion = promptVersion 형태로 저장 가능)

        return submissionRepo.save(sub);
    }
}