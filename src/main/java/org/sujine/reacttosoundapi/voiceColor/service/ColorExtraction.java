package org.sujine.reacttosoundapi.voiceColor.service;

import org.sujine.reacttosoundapi.voiceColor.domain.VoiceStream;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;

import java.awt.*;
import java.util.concurrent.Callable;

public class ColorExtractionService implements Callable<ResponseRGB[]> {
    private final RequestAudioStreamData streamData;

    public ColorExtractionService(RequestAudioStreamData streamData) {
        this.streamData = streamData;
    }

    public ResponseRGB[] call() throws IllegalArgumentException {
        System.out.println("Running on thread: " + Thread.currentThread().getName());
        VoiceStream voiceStream = new VoiceStream(this.streamData.getRawStream(), this.streamData.getSampleRate());
        double[][] frequenciesWithMagnitude = voiceStream.extractFrequency();

        ResponseRGB[] ResponseRGBs = new ResponseRGB[frequenciesWithMagnitude.length];
        for(int i = 0; i < frequenciesWithMagnitude.length; i++){
            Color colors = frequencyToColor(frequenciesWithMagnitude[i][1], frequenciesWithMagnitude[i][0]);
            ResponseRGBs[i] = new ResponseRGB(colors.getRed(), colors.getGreen(), colors.getBlue(), frequenciesWithMagnitude[i][0]);
        }
        return ResponseRGBs;
    }

    private static Color frequencyToColor(double frequency, double magnitude) {
        double minFrequency = 100.0;    // minimum audio frequency
        double maxFrequency = 1000.0;   // maximum audio frequency
        double maxMagnitude = 50;       // for use in brightness and saturation
        double normalizedFrequency = (frequency - minFrequency) / (maxFrequency - minFrequency);
        float saturation = (float)Math.min(1.0, magnitude / maxMagnitude);
        float brightness = (float) 1.0 - saturation;

        return Color.getHSBColor((float)normalizedFrequency, saturation, brightness);
    }
}
