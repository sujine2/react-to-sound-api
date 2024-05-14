package org.sujine.reacttosoundapi.voiceColor.service;

import org.sujine.reacttosoundapi.voiceColor.dto.RawAudioStream;
import org.sujine.reacttosoundapi.voiceColor.utils.AudioStreamParser;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class VoceColorService {
    void getVoiceColor(byte[] rawStream) {
        try{
            AudioStreamParser parser = new AudioStreamParser();
            AudioFormat audioFormat = parser.extractAudioFormat(rawStream);
            RawAudioStream rawAudioStream = new RawAudioStream(
                    rawStream,
                    audioFormat.getSampleSizeInBits(),
                    audioFormat.isBigEndian()
            );
            // convert byte type audio stream to double type
            double[] stream = parser.convertStreamToDoubleArray(rawAudioStream);

        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
