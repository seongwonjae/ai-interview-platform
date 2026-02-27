package com.portfolio.interview.api;

import com.portfolio.interview.api.dto.*;
import com.portfolio.interview.domain.user.UserEntity;
import com.portfolio.interview.repo.EvaluationRepository;
import com.portfolio.interview.repo.SubmissionRepository;
import com.portfolio.interview.service.EvaluationService;
import com.portfolio.interview.service.SubmissionService;
import com.portfolio.interview.service.AiEvaluator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final SubmissionRepository submissionRepo;
    private final EvaluationRepository evalRepo;
    private final AiEvaluator aiEvaluator;


    public SubmissionController(
            SubmissionService submissionService,
            SubmissionRepository submissionRepo,
            EvaluationRepository evalRepo,
            AiEvaluator aiEvaluator
    ) {
        this.submissionService = submissionService;
        this.submissionRepo = submissionRepo;
        this.evalRepo = evalRepo;
        this.aiEvaluator = aiEvaluator;
    }

    @PostMapping("/sessions/{sessionId}/questions/{questionId}/submit")
    public SubmissionDto.SubmitRes submit(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @RequestBody @Valid SubmissionDto.SubmitReq req
    ) {
        if (user == null || user.id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }

       
        var sub = submissionService.submit(
                user,
                sessionId,
                questionId,
                req.answer_text,
                req.prompt_version
        );

       
        aiEvaluator.evaluateAsync(sub.id, req.prompt_version);

        return new SubmissionDto.SubmitRes(sub.id, sub.status.name());
    }

    @GetMapping("/submissions/{submissionId}/status")
    public SubmissionDto.StatusRes status(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long submissionId
    ) {
        if (user == null || user.id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }

        var sub = submissionRepo.findByIdAndUser_Id(submissionId, user.id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden"));

        // ✅ 점수는 ai_evaluations 기준으로 조회
        Integer overallScore = sub.overallScore;
        if (overallScore == null) {
            overallScore = evalRepo.findBySubmission_Id(sub.id)
                    .map(e -> e.overallScore)
                    .orElse(null);
        }


        return new SubmissionDto.StatusRes(
                sub.id,
                sub.status.name(),
                sub.errorMessage,
                overallScore,
                sub.feedback
        );
    }

    @GetMapping("/submissions/{submissionId}/evaluation")
    public EvaluationDto.Res evaluation(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long submissionId
    ) {
        if (user == null || user.id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }

        var eval = evalRepo.findBySubmission_IdAndSubmission_User_Id(submissionId, user.id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evaluation not ready"));

        List<String> strengths = JsonUtil.parseStringArray(eval.strengthsJson);
        List<String> improvements = JsonUtil.parseStringArray(eval.improvementsJson);

        return new EvaluationDto.Res(
                submissionId,
                eval.overallScore,
                new EvaluationDto.Scores(
                        eval.scoreStructure,
                        eval.scoreClarity,
                        eval.scoreRelevance
                ),
                strengths,
                improvements,
                eval.rewrittenAnswer,
                eval.promptVersion,
                eval.createdAt.toString()
        );
    }
}
