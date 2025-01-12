package org.sujine.reacttosoundapi.voiceColor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RequestAudioStreamData {
    private double[] rawStream;
    private float sampleRate;
    private int sampleSize;
    private int channel;
    private boolean bigEndian;
}
