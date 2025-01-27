package org.sujine.reacttosoundapi.qna.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name="qna")
@NoArgsConstructor
public class Qna {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String question;
    @Column(columnDefinition = "TEXT")
    private String answer;
    private String userId;

    public Qna(String question, String answer, String userId) {
        this.question = question;
        this.answer = answer;
        this.userId = userId;
    }
}
