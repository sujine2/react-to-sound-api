package org.sujine.reacttosoundapi.voiceColor.domain;

import lombok.Getter;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.jitsi.webrtcvadwrapper.WebRTCVad;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

@Getter
public class Voice {
    private final double[] stream;
    private final float sampleRate;
    private double[][] frequenciesWithMagnitude;
    private double[] frequencies;
    private int mode = 3;

    public Voice(double[] rawStream, float sampleRate) {
        this.sampleRate = sampleRate;
        ArrayList<double[]> onlyVoice = new ArrayList<>();

        double frameDuration = 0.01; // 10ms
        int frameSize = (int)(sampleRate * frameDuration);
        WebRTCVad vad = new WebRTCVad((int)sampleRate, mode);

        for (int start = 0; start < rawStream.length; start += frameSize) {
            int stop = (int)Math.min(start + frameSize, rawStream.length);
            double[] frame = Arrays.copyOfRange(rawStream, start, stop);
            if(vad.isSpeech(frame)) {
                onlyVoice.add(frame);
            }
        }
        this.stream = onlyVoice.stream().flatMapToDouble(Arrays::stream).toArray();
    }

    public double[][]  extractFrequency() {
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] complexResult = fft.transform(this.stream, TransformType.FORWARD);
        double[][] frequencyAndMagnitude = new double[complexResult.length][];

        for (int i = 0; i < complexResult.length / 2; i++) {  // Nyquist Frequency
            double magnitude = complexResult[i].abs();        // amplitude
            double frequency = i * this.sampleRate / this.stream.length;
            frequencyAndMagnitude[i] = new double[]{magnitude, frequency};
        }
        Arrays.sort(frequencyAndMagnitude, (a, b) -> Double.compare(b[0], a[0]));
        this.frequenciesWithMagnitude = frequencyAndMagnitude;
        this.frequencies = Arrays.stream(frequencyAndMagnitude).mapToDouble(row -> row[0]).toArray();
        return this.frequenciesWithMagnitude;
    }

    public static Color frequencyToColor(double frequency, double magnitude) {
        double minFrequency = 20.0;    // minimum audio frequency
        double maxFrequency = 20000.0; // maximum audio frequency
        double maxMagnitude = 1.0;     // for use in brightness and saturation

        double normalizedFrequency = (frequency - minFrequency) / (maxFrequency - minFrequency);
        float saturation = (float)Math.min(1.0, magnitude / maxMagnitude);
        float value = saturation; // 명도도 동일하게 설정

        return Color.getHSBColor((float)normalizedFrequency, saturation, value);
    }
}
