package org.sujine.reacttosoundapi.voiceColor.utils;

import org.sujine.reacttosoundapi.voiceColor.dto.RawAudioStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioStreamParser {
    public AudioFormat extractAudioFormat(byte[] rawStream) throws UnsupportedAudioFileException, IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(rawStream);
        AudioInputStream ais = AudioSystem.getAudioInputStream(bais);
        return ais.getFormat();
    }

    public double[] convertStreamToDoubleArray(RawAudioStream rawStream) throws IllegalArgumentException{
        double[] audioData = new double[rawStream.getRawStream().length / (rawStream.getBitSize()/8)];
        ByteBuffer buffer = ByteBuffer.wrap(rawStream.getRawStream());
        buffer.order(rawStream.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

        if (rawStream.getBitSize() == 16)
            return convert16BitToDouble(audioData, buffer);
        else if (rawStream.getBitSize() == 24)
            return convert24BitToDouble(audioData, buffer, rawStream.isBigEndian());
        else throw new IllegalArgumentException("Number must be between 16 and 24");
    }

    private double[] convert16BitToDouble(double[] audioData, ByteBuffer buffer) {
        for (int i = 0; i < audioData.length; i++) {
            short sample = buffer.getShort();
            audioData[i] = sample / 32768.0; // -32768 to 32767 to -1.0 to 1.0
        }
        return audioData;
    }

    private double[] convert24BitToDouble(double[] audioData, ByteBuffer buffer, boolean isBigEndian) {
        for (int i = 0; i < audioData.length; i++) {
            int sample = 0;
            if (isBigEndian) {
                sample |= (buffer.get() & 0xFF) << 16;
                sample |= (buffer.get() & 0xFF) << 8;
                sample |= (buffer.get() & 0xFF);
            } else {
                sample |= (buffer.get() & 0xFF);
                sample |= (buffer.get() & 0xFF) << 8;
                sample |= (buffer.get() & 0xFF) << 16;
            }
            // Convert unsigned to signed
            if (sample > 0x7FFFFF) sample -= 0x1000000;
            audioData[i] = sample / 8388608.0; // -8388608 to 8388607 to -1.0 to 1.0
        }
        return audioData;
    }

}


