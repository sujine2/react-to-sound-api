package org.sujine.reacttosoundapi.stt.dto;
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
    boolean last;
}
