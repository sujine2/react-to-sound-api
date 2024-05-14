package org.sujine.reacttosoundapi.voiceColor.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RequestRawAudioStream {
    private byte[] stream;
}
