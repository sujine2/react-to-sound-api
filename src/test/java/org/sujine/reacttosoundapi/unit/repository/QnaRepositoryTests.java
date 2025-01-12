package org.sujine.reacttosoundapi.unit.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.sujine.reacttosoundapi.qna.domain.Qna;
import org.sujine.reacttosoundapi.qna.repository.QnaRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
public class QnaRepositoryTests {
    @Autowired
    private QnaRepository qnaRepository;

    private String userId = "test";

    @DisplayName("check find by user id")
    @Test
    void findByUserIdTest() {
        Qna qna1 = new Qna("what is your favorite color?", "yellow", userId);
        Qna qna2 = new Qna("What is Spring Boot?", "spring", userId);
        Qna qna3 = new Qna("Do you have project?", "yes", userId);

        qnaRepository.save(qna1);
        qnaRepository.save(qna2);
        qnaRepository.save(qna3);

        List<Qna> userQnas = qnaRepository.findByUserId(userId);

        assertThat(userQnas).hasSize(3);
        assertThat(userQnas.get(0).getQuestion()).isEqualTo("what is your favorite color?");
        assertThat(userQnas.get(0).getAnswer()).isEqualTo("yellow");
        assertThat(userQnas.get(2).getQuestion()).isEqualTo("Do you have project?");
        assertThat(userQnas.get(2).getAnswer()).isEqualTo("yes");

        List<Qna> emptyQnas = qnaRepository.findByUserId("testt");
        assertThat(emptyQnas).hasSize(0);
    }

}
