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
    public static AudioFormat extractAudioFormat(byte[] rawStream) throws UnsupportedAudioFileException, IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(rawStream);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
        return audioInputStream.getFormat();
    }

    public static double[] convertStreamToDoubleArray(AudioFormat audioFormat) throws IllegalArgumentException{
        double[] audioData = new double[rawStream.getRawStream().length / (rawStream.getBitSize()/8)];
        ByteBuffer buffer = ByteBuffer.wrap(rawStream.getRawStream());

        if (rawStream.getBitSize() == 16) {
            buffer.order(rawStream.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            return convert16BitToDouble(audioData, buffer);
        } else if (rawStream.getBitSize() == 24)
            return convert24BitToDouble(audioData, buffer, rawStream.isBigEndian());
        else if (rawStream.getBitSize() == 32)
            return convert32BitToDouble(audioData, buffer);
        else throw new IllegalArgumentException("Number must be between 16 and 24");
    }

    public static byte[] convertDoubleToByteArray(double[] stream, int bitSize, boolean isBigEndian) throws IllegalArgumentException {
        int byteUnit = bitSize / 8;
        byte[] byteArray = new byte[stream.length * byteUnit];
        if (bitSize == 16)
            return convertDoubleTo16Bit(stream, byteArray, byteUnit, isBigEndian);
        else if (bitSize == 24)
            return convertDoubleTo24Bit(stream, byteArray, byteUnit, isBigEndian);
        else if (bitSize == 32)
            return convertDoubleTo32Bit(stream, byteArray, byteUnit, isBigEndian);
        else throw new IllegalArgumentException("Number must be between 16 and 24");
    }

    private static double[] convert16BitToDouble(double[] audioData, ByteBuffer buffer) {
        for (int i = 0; i < audioData.length; i++) {
            short sample = buffer.getShort();
            audioData[i] = sample / 32768.0; // -32768 to 32767 to -1.0 to 1.0
        }
        return audioData;
    }

    private static double[] convert24BitToDouble(double[] audioData, ByteBuffer buffer, boolean isBigEndian) {
        for (int i = 0; i < audioData.length; i++) {
            int sample = 0;
            if (isBigEndian)
                sample = ((buffer.get() & 0xFF) << 16 | (buffer.get() & 0xFF) << 8 | (buffer.get() & 0xFF));
            else
                sample = ((buffer.get() & 0xFF) | (buffer.get() & 0xFF) << 8 | (buffer.get() & 0xFF) << 16);

            // Convert unsigned to signed
            if (sample >= 0x800000) sample -= 0x1000000;
            audioData[i] = sample / 8388608.0; // -8388608 to 8388607 to -1.0 to 1.0
        }
        return audioData;
    }

    private static double[] convert32BitToDouble(double[] audioData, ByteBuffer buffer) {
        for (int i = 0; i < audioData.length; i++) {
            int sample = buffer.getInt();
            audioData[i] = sample / 2147483648.0; // -2147483648 to 2147483647 to -1.0 to 1.0
        }
        return audioData;
    }

    private static byte[] convertDoubleTo16Bit(double[] audioData, byte[] byteArray, int byteUnit, boolean isBigEndian) {
        for (int i = 0; i < audioData.length; i++) {
            int sample = (int) (audioData[i] * 32767); // -1.0 ~ 1.0 to -32768 ~ 32767

            if (isBigEndian) {
                byteArray[byteUnit * i + 1] = (byte) (sample & 0xFF);
                byteArray[byteUnit * i] = (byte) ((sample >> 8) & 0xFF);
            } else {
                byteArray[byteUnit * i] = (byte) (sample & 0xFF);
                byteArray[byteUnit * i + 1] = (byte) ((sample >> 8) & 0xFF);
            }
        }
        return byteArray;
    }

    private static byte[] convertDoubleTo24Bit(double[] audioData, byte[] byteArray, int byteUnit, boolean isBigEndian) {
        for (int i = 0; i < audioData.length; i++) {
            int sample = (int) (audioData[i] * 8388607); // -1.0 ~ 1.0 to -8388608 ~ 8388607

            if (isBigEndian) {
                byteArray[byteUnit * i + 2] = (byte) (sample & 0xFF);
                byteArray[byteUnit * i + 1] = (byte) ((sample >> 8) & 0xFF);
                byteArray[byteUnit * i] = (byte) ((sample >> 16) & 0xFF);
            } else {
                byteArray[byteUnit * i] = (byte) (sample & 0xFF);
                byteArray[byteUnit * i + 1] = (byte) ((sample >> 8) & 0xFF);
                byteArray[byteUnit * i + 2] = (byte) ((sample >> 16) & 0xFF);
            }
        }
        return byteArray;
    }

    public static byte[] convertDoubleTo32Bit(double[] audioData, byte[] byteArray, int byteUnit, boolean isBigEndian) {
        for (int i = 0; i < audioData.length; i++) {
            int sample = (int) (audioData[i] * 2147483647); // -1.0 ~ 1.0 to -2147483648 ~ 2147483647

            if (isBigEndian) {
                byteArray[byteUnit * i] = (byte) ((sample >> 24) & 0xFF);
                byteArray[byteUnit * i + 1] = (byte) ((sample >> 16) & 0xFF);
                byteArray[byteUnit * i + 2] = (byte) ((sample >> 8) & 0xFF);
                byteArray[byteUnit * i + 3] = (byte) (sample & 0xFF);
            } else {
                byteArray[byteUnit * i] = (byte) (sample & 0xFF);
                byteArray[byteUnit * i + 1] = (byte) ((sample >> 8) & 0xFF);
                byteArray[byteUnit * i + 2] = (byte) ((sample >> 16) & 0xFF);
                byteArray[byteUnit * i + 3] = (byte) ((sample >> 24) & 0xFF);
            }
        }
        return byteArray;
    }
}


