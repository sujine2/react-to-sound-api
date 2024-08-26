package org.sujine.reacttosoundapi.speechToText.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter

public class ResponseAudioText {
    String result;
    boolean isFinal;

    @Override
    public String toString() {
        return "(" + result + "," + isFinal + ")";
    }
}

