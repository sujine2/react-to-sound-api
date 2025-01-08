package org.sujine.reacttosoundapi.qa.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter

public class ResponseText {
    String result;
    boolean isResponse;
    boolean isFinal;

    @Override
    public String toString() {
        return "(" + result + "," + isFinal + ")";
    }
}

