package org.sujine.reacttosoundapi.voiceColor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponseRGB {
    private int red;
    private int green;
    private int blue;

    @Override
    public String toString() {
        return "(" + red + "," + green + "," + blue + ")";
    }
}
