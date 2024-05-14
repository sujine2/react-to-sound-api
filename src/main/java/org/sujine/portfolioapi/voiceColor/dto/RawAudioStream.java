package org.sujine.portfolioapi.voiceColor.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class RawAudioStream {
    private byte[] rawStream;
    private int bitSize;
    private boolean isBigEndian;
}
