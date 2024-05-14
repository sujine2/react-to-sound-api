package org.sujine.portfolioapi.voiceColor.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class RequestRawStream {
    private byte[] stream;
}
