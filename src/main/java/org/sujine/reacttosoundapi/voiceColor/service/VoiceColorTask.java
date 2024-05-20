package org.sujine.reacttosoundapi.voiceColor.service;

import lombok.AllArgsConstructor;
import org.sujine.reacttosoundapi.voiceColor.domain.Voice;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;
import org.sujine.reacttosoundapi.voiceColor.utils.AudioStreamFormatter;

import java.awt.*;
import java.util.concurrent.Callable;

@AllArgsConstructor
public class VoiceColorTask implements Callable<ResponseRGB[]> {
    private RequestAudioStreamData streamData;

    public ResponseRGB[] call() throws IllegalArgumentException {
        // convert byte type audio stream to double type
        System.out.println("thread id #" + Thread.currentThread().threadId());
        double[] stream = AudioStreamFormatter.convertStreamToDoubleArray(
                this.streamData.getRawStream(),
                this.streamData.getSampleSize(),
                this.streamData.isBigEndian()
        );

        Voice voice = new Voice(stream, this.streamData.getSampleRate());
        double[][] frequenciesWithMagnitude = voice.extractFrequency();

        ResponseRGB[] ResponseRGBs = new ResponseRGB[frequenciesWithMagnitude.length];
        for(int i = 0; i < frequenciesWithMagnitude.length; i++){
            Color colors = Voice.frequencyToColor(frequenciesWithMagnitude[i][1], frequenciesWithMagnitude[i][0]);
            ResponseRGBs[i] = new ResponseRGB(colors.getRed(), colors.getGreen(), colors.getBlue());
        }
        return ResponseRGBs;
    }
}
