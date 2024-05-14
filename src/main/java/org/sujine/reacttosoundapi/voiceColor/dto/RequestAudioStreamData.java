package org.sujine.reacttosoundapi.voiceColor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RequestAudioStreamData {
    byte[] rawStream;
    float sampleRate;
    int sampleSize;
    int channel;
    boolean bigEndian;
}
