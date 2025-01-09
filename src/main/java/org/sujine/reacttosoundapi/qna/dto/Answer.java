package org.sujine.reacttosoundapi.qna.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Answer {
    String result;
    boolean isResponse;
    boolean isFinal;

    @Override
    public String toString() {
        return "(" + result + "," + isFinal + ")";
    }
}

