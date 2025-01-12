package org.sujine.reacttosoundapi.voiceColor.domain.utils;

public class AudioStreamFormatter {
    public static double[] padArray(double[] array, int length) {
        if (array.length == length) {
            return array;
        }
        double[] paddedArray = new double[length];
        System.arraycopy(array, 0, paddedArray, 0, array.length);

        return paddedArray;
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


