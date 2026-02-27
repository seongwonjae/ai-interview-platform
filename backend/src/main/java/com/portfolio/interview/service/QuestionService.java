package com.portfolio.interview.service;

import com.portfolio.interview.domain.enums.*;
import com.portfolio.interview.domain.interview.InterviewQuestionEntity;
import com.portfolio.interview.repo.QuestionRepository;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {
    private final QuestionRepository repo;

    public QuestionService(QuestionRepository repo) {
        this.repo = repo;
    }

    public InterviewQuestionEntity recommend(Role role, Difficulty difficulty) {
        return repo.findRandom(role, difficulty).orElseThrow(() -> new RuntimeException("No question found"));
    }
}
