package org.sujine.reacttosoundapi.qna.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class QuestionAudioStream {
    byte[] rawStream;
    float sampleRate;
    int sampleSize;
    int channel;
    boolean bigEndian;
    boolean last;
}
