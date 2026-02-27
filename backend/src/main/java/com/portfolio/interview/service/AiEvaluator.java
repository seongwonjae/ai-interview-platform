package com.portfolio.interview.service;

public interface AiEvaluator {
    void evaluateAsync(Long submissionId, String promptVersion);
}