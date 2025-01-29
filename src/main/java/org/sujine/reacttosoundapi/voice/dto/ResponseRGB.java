package org.sujine.reacttosoundapi.audio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponseRGB {
    private int red;
    private int green;
    private int blue;
    private double magnitude;

    @Override
    public String toString() {
        return "(" + red + "," + green + "," + blue + "," + magnitude + ")";
    }
}
