package org.sujine.reacttosoundapi.voiceColor.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioStreamFormatter {
    public static double[] convertStreamToDoubleArray(byte[] rawStream, int sampleSize, boolean isBigEndian)
            throws IllegalArgumentException{
        double[] audioData = new double[rawStream.length / (sampleSize/8)];
        ByteBuffer buffer = ByteBuffer.wrap(rawStream);

        if (sampleSize == 16) {
            buffer.order(isBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            return convert16BitToDouble(audioData, buffer);
        } else if (sampleSize == 32) {
            buffer.order(isBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            return convert32BitToDouble(audioData, buffer);
        } else throw new IllegalArgumentException("Number must be between 16 and 24");
    }

    public static byte[] convertDoubleToByteArray(double[] stream, int sampleSize, boolean isBigEndian) throws IllegalArgumentException {
        int byteUnit = sampleSize / 8;
        byte[] byteArray = new byte[stream.length * byteUnit];
        if (sampleSize == 16)
            return convertDoubleTo16Bit(stream, byteArray, byteUnit, isBigEndian);
        else if (sampleSize == 32)
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

    public static double[] padArrayToNextPowerOfTwo(double[] inputStream) {
        int originalLength = inputStream.length;
        int newLength = findNextPowerOfTwo(originalLength);
        double[] paddedArray = new double[newLength];
        System.arraycopy(inputStream, 0, paddedArray, 0, originalLength);
        return paddedArray;
    }

    public static int findNextPowerOfTwo(int input) {
        int result = 1;
        while (result < input) {
            result <<= 1;
        }
        return result;
    }
}


