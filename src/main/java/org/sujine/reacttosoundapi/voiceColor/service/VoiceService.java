package org.sujine.reacttosoundapi.voiceColor.service;

import org.sujine.reacttosoundapi.voiceColor.domain.Voice;
import org.sujine.reacttosoundapi.voiceColor.dto.RawAudioStream;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestRawAudioStream;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;
import org.sujine.reacttosoundapi.voiceColor.utils.AudioStreamParser;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.*;
import java.io.IOException;

public class VoceColorService {
    public static ResponseRGB[] getMainVoiceColor(RequestRawAudioStream rawStream) {
        try{
            AudioFormat audioFormat = AudioStreamParser.extractAudioFormat(rawStream.getStream());
            RawAudioStream rawAudioStream = new RawAudioStream(
                    rawStream.getStream(),
                    audioFormat.getSampleSizeInBits(),
                    audioFormat.isBigEndian()
            );
            // convert byte type audio stream to double type
            double[] stream = AudioStreamParser.convertStreamToDoubleArray(rawAudioStream);

            Voice voice = new Voice(stream, audioFormat.getSampleRate());
            double[][] frequenciesWithMagnitude = voice.extractFrequency();

            ResponseRGB[] RGBColors = new ResponseRGB[5];
            for(int i = 0; i < 5; i++){  // picks five frequency
                Color colors = Voice.frequencyToColor(frequenciesWithMagnitude[i][1], frequenciesWithMagnitude[i][0]);
                RGBColors[i] = new ResponseRGB(colors.getRed(), colors.getGreen(), colors.getBlue());
            }
            return RGBColors;
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
