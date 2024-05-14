package org.sujine.reacttosoundapi.voiceColor.domain;

import lombok.Getter;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.jitsi.webrtcvadwrapper.WebRTCVad;

import java.util.ArrayList;
import java.util.Arrays;

@Getter
public class Voice {
    private final double[] stream;
    private final float sampleRate;
    private double[] frequencies;
    private int mode = 3;

    Voice(double[] rawStream, float sampleRate) {
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

    double[] extractFrequency() {
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] complexResult = fft.transform(this.stream, TransformType.FORWARD);
        ArrayList<double[]> frequencyAndMagnitude = new ArrayList<>();

        for (int i = 0; i < complexResult.length; i++) {
            double magnitude = complexResult[i].abs();
            double frequency = i * this.sampleRate / this.stream.length;
            frequencyAndMagnitude.add(new double[]{magnitude, frequency});
        }
        Arrays.sort(frequencyAndMagnitude, (a, b) -> Double.compare(b[0], a[0]));
        this.frequencies = frequencyAndMagnitude.stream().mapToDouble(x -> x[1]).toArray();
        return this.frequencies;
    }


}
