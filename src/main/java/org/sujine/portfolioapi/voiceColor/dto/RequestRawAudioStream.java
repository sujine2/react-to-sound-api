package org.sujine.portfolioapi.voiceColor.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class RequestRawAudioStream {
    private byte[] stream;
}
