package org.sujine.reacttosoundapi.voice.domain;

import lombok.Getter;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.jitsi.webrtcvadwrapper.WebRTCVad;
import org.sujine.reacttosoundapi.voice.utils.AudioStreamFormatter;

import java.util.ArrayList;
import java.util.Arrays;

@Getter
public class Voice {
    private final double[] stream;
    private final float sampleRate;
    private static final int mode = 3;
    private static final int fftSize = 1024;
    //    private double[] frequencies;

    public Voice(double[] rawStream, float sampleRate) {
        this.sampleRate = sampleRate;
        ArrayList<double[]> onlyVoice = new ArrayList<>();

        double frameDuration = 0.01; // 10ms
        int frameSize = (int)(sampleRate * frameDuration);
        WebRTCVad vad = new WebRTCVad((int)sampleRate, mode);
        for (int start = 0; start < rawStream.length; start += frameSize) {
            int stop = (int)Math.min(start + frameSize, rawStream.length);
            double[] frame = AudioStreamFormatter.padArray(Arrays.copyOfRange(rawStream, start, stop), frameSize);
            if(vad.isSpeech(frame)) onlyVoice.add(frame);
        }
        this.stream = onlyVoice.stream().flatMapToDouble(Arrays::stream).toArray();
    }

    public double[][]  extractFrequency() {
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        int streamLength = this.stream.length;
        ArrayList<ArrayList<Double>> frequencyAndMagnitude = new ArrayList<>();

        for (int start = 0; start <= streamLength; start += this.fftSize) {
            ArrayList<Double> row = new ArrayList<>();
            int end = (int)Math.min(start + this.fftSize, streamLength);
            int size = (end != streamLength)? this.fftSize: this.fftSize + streamLength - (start + this.fftSize);
            double[] segment = new double[size];
            System.arraycopy(this.stream, start, segment, 0, size);

            if (end == streamLength) segment = AudioStreamFormatter.padArrayToNextPowerOfTwo(segment);
            Complex[] complexResult = fft.transform(segment, TransformType.FORWARD);
            double maxMagnitude = 0;
            double maxFrequency = 0;

            for (int i = 0; i < complexResult.length / 2; i++) {
                double magnitude = complexResult[i].abs();
                if (magnitude > maxMagnitude) {
                    maxMagnitude = magnitude;
                    maxFrequency = i * this.sampleRate / this.fftSize;
                }
            }
            row.add(maxMagnitude);
            row.add(maxFrequency);
            frequencyAndMagnitude.add(row);
        }
//        double[][] frequenciesWithMagnitude = frequencyAndMagnitude.stream()
//                .map(list -> list.stream().mapToDouble(Double::doubleValue).toArray())
//                .toArray(double[][]::new);
//        this.frequencies = frequencyAndMagnitude.stream().mapToDouble(row -> row.get(1)).toArray();
        return frequencyAndMagnitude.stream()
                .map(list -> list.stream().mapToDouble(Double::doubleValue).toArray())
                .toArray(double[][]::new);
    }
}
