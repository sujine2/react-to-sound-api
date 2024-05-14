package org.sujine.reacttosoundapi.voiceColor.service;

import org.jitsi.webrtcvadwrapper.WebRTCVad;
import org.junit.runner.RunWith;
import org.sujine.reacttosoundapi.voiceColor.domain.Voice;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;
import org.sujine.reacttosoundapi.voiceColor.utils.AudioStreamFormatter;

import java.awt.*;

public class VoiceService {
    public static ResponseRGB[] getMainVoiceColor(RequestAudioStreamData streamData) throws IllegalArgumentException {
        // convert byte type audio stream to double type
        double[] stream = AudioStreamFormatter.convertStreamToDoubleArray(
                streamData.getRawStream(),
                streamData.getSampleSize(),
                streamData.isBigEndian()
        );

        Voice voice = new Voice(stream, streamData.getSampleRate());
        double[][] frequenciesWithMagnitude = voice.extractFrequency();

        ResponseRGB[] ResponseRGBs = new ResponseRGB[5];
        for(int i = 0; i < 5; i++){  // picks five frequency
            Color colors = Voice.frequencyToColor(frequenciesWithMagnitude[i][1], frequenciesWithMagnitude[i][0]);
            ResponseRGBs[i] = new ResponseRGB(colors.getRed(), colors.getGreen(), colors.getBlue());
        }
        return ResponseRGBs;
    }

    public static byte[] getVad(RequestAudioStreamData streamData) throws IllegalArgumentException {
        // convert byte type audio stream to double type
        double[] stream = AudioStreamFormatter.convertStreamToDoubleArray(
                streamData.getRawStream(),
                streamData.getSampleSize(),
                streamData.isBigEndian()
        );

        Voice voice = new Voice(stream, streamData.getSampleRate());
        return AudioStreamFormatter.convertDoubleToByteArray(
                voice.getStream(),
                streamData.getSampleSize(),
                streamData.isBigEndian()
        );
    }
}
