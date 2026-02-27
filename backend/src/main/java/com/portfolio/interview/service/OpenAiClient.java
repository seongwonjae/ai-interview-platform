package com.portfolio.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiClient {

    private final RestClient client;
    private final String model;
    private final ObjectMapper om = new ObjectMapper();

    public OpenAiClient(
            @Value("${openai.apiKey:}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model
    ) { 
        if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("openai.apiKey is missing (ai.provider=openai)");
        }
        
        this.model = model;
        this.client = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> evaluate(String systemPrompt, String userPrompt) {

        // ✅ "schema"는 '순수 JSON Schema'만
        Map<String, Object> jsonSchema = Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "overall_score", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                        "scores", Map.of(
                                "type", "object",
                                "additionalProperties", false,
                                "properties", Map.of(
                                        "structure", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                                        "clarity", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                                        "relevance", Map.of("type", "integer", "minimum", 0, "maximum", 100)
                                ),
                                "required", List.of("structure", "clarity", "relevance")
                        ),
                        "strengths", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string"),
                                "minItems", 1
                        ),
                        "improvements", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string"),
                                "minItems", 1
                        ),
                        "rewritten_answer", Map.of("type", "string")
                ),
                "required", List.of("overall_score", "scores", "strengths", "improvements", "rewritten_answer")
        );

        // ✅ 여기 중요: text.format.name / schema / strict 를 "직접" 넣기
        Map<String, Object> body = Map.of(
                "model", model,
                "input", List.of(
                        Map.of("role", "system", "content", List.of(
                                Map.of("type", "input_text", "text", systemPrompt)
                        )),
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "input_text", "text", userPrompt)
                        ))
                ),
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "InterviewEval",   // ✅ 필수 (에러 원인)
                                "strict", true,           // ✅ 권장
                                "schema", jsonSchema      // ✅ 필수
                        )
                )
        );

        Map<String, Object> res = client.post()
                .uri("/responses")
                .body(body)
                .retrieve()
                .body(Map.class);

        String json = extractFirstText(res);
        try {
            return om.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse model json: " + e.getMessage() + "\nraw=" + json, e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractFirstText(Map<String, Object> res) {
        var output = (List<Object>) res.get("output");
        if (output == null || output.isEmpty()) {
            throw new RuntimeException("OpenAI response has no output: " + res);
        }

        var first = (Map<String, Object>) output.get(0);
        var content = (List<Object>) first.get("content");
        if (content == null || content.isEmpty()) {
            throw new RuntimeException("OpenAI response has no content: " + res);
        }

        var c0 = (Map<String, Object>) content.get(0);
        var text = (String) c0.get("text");
        if (text == null) {
            throw new RuntimeException("OpenAI response content[0].text is null: " + res);
        }
        return text;
    }
}