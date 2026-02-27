package com.portfolio.interview.service;

import com.portfolio.interview.repo.SubmissionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiEvaluationService implements AiEvaluator {

    private final OpenAiClient openAiClient;
    private final EvaluationService evaluationService;
    private final SubmissionRepository submissionRepo;

    public OpenAiEvaluationService(
            OpenAiClient openAiClient,
            EvaluationService evaluationService,
            SubmissionRepository submissionRepo
    ) {
        this.openAiClient = openAiClient;
        this.evaluationService = evaluationService;
        this.submissionRepo = submissionRepo;
    }

    @Override
    @Async
    public void evaluateAsync(Long submissionId, String promptVersion) {

        Instant start = Instant.now();

        try {
            // 🔥 OpenAI 호출 (트랜잭션 아님)
            Map<String, Object> result =
                    openAiClient.evaluate(buildSystemPrompt(), buildUserPrompt(submissionId));

            long latency = Duration.between(start, Instant.now()).toMillis();
            System.out.println("[OpenAI] latency(ms)=" + latency + " submissionId=" + submissionId);

            // 🔥 DB 저장은 별도 트랜잭션 메서드
            evaluationService.saveSuccess(submissionId, promptVersion, result);

        } catch (Exception e) {

            long latency = Duration.between(start, Instant.now()).toMillis();
            System.out.println("[OpenAI] FAILED latency(ms)=" + latency + " submissionId=" + submissionId);

            evaluationService.saveFailure(submissionId, e);
        }
    }

    private String buildSystemPrompt() {
        return """
    You are a strict senior technical interviewer.

    You must return ONLY valid JSON that strictly follows the provided schema.
    All natural language output must be written in KOREAN.

    Evaluation rules:
    - Be conservative in scoring.
    - Do NOT give high scores unless the answer is technically strong and well-structured.
    - strengths: 1–3 concise sentences describing what is good.
    - improvements: 1–3 concrete and actionable suggestions explaining what and how to improve.
    - overall_score and sub-scores must reflect technical accuracy, structure, clarity, and relevance.

    Important rules for "rewritten_answer" (model answer):
    - rewritten_answer must contain ONLY the model answer content.
    - Do NOT include scoring, evaluation, feedback, strengths, weaknesses, or improvement language.
    - Do NOT use words like "점수", "평가", "피드백", "개선", "장점", "단점", "총평".
    - Do NOT give advice (e.g., "~하면 좋습니다", "~해야 합니다").
    - Write as if you are the candidate answering in a real interview.
    - Use a natural spoken tone.
    - Write 5–7 sentences.
    """;
    }

    // ✅ submissionId로부터 질문/답변을 뽑아서 기존 buildUserPrompt(String,String)로 연결
    private String buildUserPrompt(Long submissionId) {
        var sub = submissionRepo.findByIdWithQuestion(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));

        String questionText = sub.question.text;
        String answerText = sub.answerText;

        return buildUserPrompt(questionText, answerText);
    }

    private String buildUserPrompt(String question, String answer) {
        return """
    [Interview Question]
    %s

    [Candidate Answer]
    %s

    Your tasks:
    1) Evaluate the candidate answer.
    2) Provide strengths, improvements, and scores.
    3) Generate a high-quality model answer in "rewritten_answer".

    Remember:
    - Evaluation content goes into strengths, improvements, and scores.
    - The model answer goes ONLY into rewritten_answer.
    """.formatted(question, answer);
    }
}