package org.sujine.reacttosoundapi.stt.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Response {
    String result;
    boolean isFinal;

    public String toString() {
        return "(" + result + "," + isFinal + ")";
    }
}

