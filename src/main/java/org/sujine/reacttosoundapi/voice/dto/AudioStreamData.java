package org.sujine.reacttosoundapi.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class RequestAudioStreamData {
    private double[] rawStream;
    private float sampleRate;
    private int sampleSize;
    private int channel;
    private boolean bigEndian;
}
