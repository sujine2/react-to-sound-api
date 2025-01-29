package org.sujine.reacttosoundapi.qna.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sujine.reacttosoundapi.ai.AIClient;
import org.sujine.reacttosoundapi.qna.domain.ExampleQuestion;
import org.sujine.reacttosoundapi.qna.domain.Qna;
import org.sujine.reacttosoundapi.qna.dto.Answer;
import org.sujine.reacttosoundapi.qna.repository.QnaRepository;

@Service
public class QnaService {
    private final QnaRepository qnaRepository;
    private final AIClient aiClient;

    @Autowired
    public QnaService(QnaRepository qnaRepository, AIClient aiClient) {
        this.aiClient = aiClient;
        this.qnaRepository = qnaRepository;
    }

    @Transactional(readOnly = true)
    public List<Qna> getHistories(String userId) {
        return this.qnaRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<ExampleQuestion> getExampleQuestions() {
        List<Object[]> results = this.qnaRepository.findRandomQuestions(2);
        return results.stream()
                .map(result -> new ExampleQuestion(
                        ((Number) result[0]).longValue(),
                        (String) result[1]
                )).collect(Collectors.toList());
    }

    @Async
    public CompletableFuture<Answer> processQuestion(String question, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return aiClient.generateResponse(question);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenApply(answer -> {
            this.saveQna(question, answer, userId);
            return new Answer(answer);
        });
    }

    private Qna saveQna(String question, String answer, String userId) {
        return this.qnaRepository.save(new Qna(question, answer, userId));
    }
}
