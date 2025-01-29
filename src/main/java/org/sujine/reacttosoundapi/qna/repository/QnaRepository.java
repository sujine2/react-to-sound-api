package org.sujine.reacttosoundapi.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.sujine.reacttosoundapi.question.domain.Qna;

import java.util.List;

@Repository
public interface QnaRepository extends JpaRepository<Qna, Long> {
    List<Qna> findByUserId(String userId);
    @Query(value = "SELECT * FROM ex_questions ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Object[]> findRandomQuestions(int limit);
}