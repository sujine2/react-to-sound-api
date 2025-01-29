package org.sujine.reacttosoundapi.voice.service;

import org.sujine.reacttosoundapi.voice.domain.Voice;
import org.sujine.reacttosoundapi.voice.dto.AudioStreamData;
import org.sujine.reacttosoundapi.voice.dto.ResponseRGB;

import java.awt.*;
import java.util.concurrent.Callable;

public class ColorExtraction implements Callable<ResponseRGB[]> {
    private final AudioStreamData streamData;

    public ColorExtraction(AudioStreamData streamData) {
        this.streamData = streamData;
    }

    public ResponseRGB[] call() throws IllegalArgumentException {
//        System.out.println("Running on thread: " + Thread.currentThread().getName());
        Voice voice = new Voice(
                streamData.getRawStream(),
                streamData.getSampleRate()
        );
        double[][] frequenciesWithMagnitude = voice.extractFrequency();

        ResponseRGB[] ResponseRGBs = new ResponseRGB[frequenciesWithMagnitude.length];
        for(int i = 0; i < frequenciesWithMagnitude.length; i++){
            Color colors = getFrequencyToColor(frequenciesWithMagnitude[i][1], frequenciesWithMagnitude[i][0]);
            ResponseRGBs[i] = new ResponseRGB(
                    colors.getRed(),
                    colors.getGreen(),
                    colors.getBlue(),
                    frequenciesWithMagnitude[i][0]
            );
        }
        return ResponseRGBs;
    }

    private static Color getFrequencyToColor(double frequency, double magnitude) {
        double minFrequency = 100.0;    // minimum audio frequency
        double maxFrequency = 1000.0;   // maximum audio frequency
        double maxMagnitude = 50;       // for use in brightness and saturation
        double normalizedFrequency = (frequency - minFrequency) / (maxFrequency - minFrequency);
        float saturation = (float)Math.min(1.0, magnitude / maxMagnitude);
        float brightness = (float) 1.0 - saturation;

        return Color.getHSBColor((float)normalizedFrequency, saturation, brightness);
    }
}
