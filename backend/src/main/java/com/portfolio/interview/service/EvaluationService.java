package com.portfolio.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.interview.domain.enums.SubmissionStatus;
import com.portfolio.interview.domain.interview.AiEvaluationEntity;
import com.portfolio.interview.domain.interview.InterviewSubmissionEntity;
import com.portfolio.interview.repo.EvaluationRepository;
import com.portfolio.interview.repo.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class EvaluationService {

    private final SubmissionRepository submissionRepo;
    private final EvaluationRepository evalRepo;
    private final ObjectMapper om = new ObjectMapper();

    public EvaluationService(SubmissionRepository submissionRepo,
                             EvaluationRepository evalRepo) {
        this.submissionRepo = submissionRepo;
        this.evalRepo = evalRepo;
    }

    /**
     * OpenAI 평가 결과 저장 (성공 케이스)
     */
    @Transactional
    public void saveSuccess(Long submissionId,
                            String promptVersion,
                            Map<String, Object> out) {

        InterviewSubmissionEntity sub = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found: " + submissionId));

        if (sub.status != SubmissionStatus.PROCESSING) return;

        int overall = ((Number) out.get("overall_score")).intValue();

        @SuppressWarnings("unchecked")
        Map<String, Object> scores = (Map<String, Object>) out.get("scores");

        int structure = ((Number) scores.get("structure")).intValue();
        int clarity = ((Number) scores.get("clarity")).intValue();
        int relevance = ((Number) scores.get("relevance")).intValue();

        @SuppressWarnings("unchecked")
        List<String> strengths = (List<String>) out.get("strengths");

        @SuppressWarnings("unchecked")
        List<String> improvements = (List<String>) out.get("improvements");

        String rewritten = (String) out.get("rewritten_answer");

        AiEvaluationEntity eval = evalRepo.findBySubmission_Id(sub.id)
                .orElseGet(AiEvaluationEntity::new);

        eval.submission = sub;
        eval.promptVersion = promptVersion;
        eval.overallScore = overall;
        eval.scoreStructure = structure;
        eval.scoreClarity = clarity;
        eval.scoreRelevance = relevance;
        eval.strengthsJson = toJsonArray(strengths);
        eval.improvementsJson = toJsonArray(improvements);
        eval.rewrittenAnswer = rewritten == null ? "" : rewritten;
        try {
            eval.rawResponseJson = om.writeValueAsString(out);
        } catch (Exception e) {
            eval.rawResponseJson = "{}";
        }

        evalRepo.save(eval);

        sub.overallScore = overall;
        sub.feedback = buildFeedback(overall, strengths, improvements, rewritten);
        sub.status = SubmissionStatus.DONE;
        sub.errorMessage = null;
        sub.updatedAt = Instant.now();

        submissionRepo.save(sub);
    }

    /**
     * 실패 케이스 저장
     */
    @Transactional
    public void saveFailure(Long submissionId, Exception e) {
        submissionRepo.findById(submissionId).ifPresent(sub -> {
            sub.status = SubmissionStatus.FAILED;
            sub.errorMessage = e.getMessage();
            sub.updatedAt = Instant.now();
            submissionRepo.save(sub);
        });
    }

    private String buildFeedback(int overall,
                                 List<String> strengths,
                                 List<String> improvements,
                                 String rewritten) {

        StringBuilder sb = new StringBuilder();
        sb.append("(AI 평가)\n");
        sb.append("- overall: ").append(overall).append("/100\n\n");

        if (strengths != null) {
            sb.append("강점:\n");
            strengths.forEach(s -> sb.append("- ").append(s).append("\n"));
            sb.append("\n");
        }

        if (improvements != null) {
            sb.append("개선점:\n");
            improvements.forEach(i -> sb.append("- ").append(i).append("\n"));
            sb.append("\n");
        }

        if (rewritten != null && !rewritten.isBlank()) {
            sb.append("모범 답안(리라이트):\n").append(rewritten);
        }

        return sb.toString();
    }

    private String toJsonArray(List<String> items) {
        if (items == null) return "[]";
        return items.stream()
                .map(s -> "\"" + s.replace("\"", "\\\"") + "\"")
                .reduce((a, b) -> a + "," + b)
                .map(s -> "[" + s + "]")
                .orElse("[]");
    }
}