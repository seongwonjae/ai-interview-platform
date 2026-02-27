package com.portfolio.interview.service;

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

    public OpenAiEvaluationService(OpenAiClient openAiClient,
                                   EvaluationService evaluationService) {
        this.openAiClient = openAiClient;
        this.evaluationService = evaluationService;
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
You are a senior technical interviewer.
Return only valid JSON strictly following schema.
Write all natural language in KOREAN.
""";
    }

    private String buildUserPrompt(Long submissionId) {
        // 필요하면 submissionRepo 주입해서 질문/답변 가져와도 됨
        return "Evaluate submission id=" + submissionId;
    }
}