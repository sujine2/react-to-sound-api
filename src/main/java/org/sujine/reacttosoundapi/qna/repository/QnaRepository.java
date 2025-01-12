package org.sujine.reacttosoundapi.qna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sujine.reacttosoundapi.qna.domain.Qna;

import java.util.List;

@Repository
public interface QnaRepository extends JpaRepository<Qna, Long> {
    List<Qna> findByUserId(String userId);
}