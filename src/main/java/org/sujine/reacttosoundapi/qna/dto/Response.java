package org.sujine.reacttosoundapi.qna.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Response {
    String result;
    boolean isAnswer;
    boolean isFinal;

    @Override
    public String toString() {
        return "(" + result + "," + isFinal + ")";
    }
}

