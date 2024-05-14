package org.sujine.reacttosoundapi;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CreateSampleData {
    public static byte[] createVoiceRawStream(float sampleRate, int sampleSizeInBits, boolean isBigEndian) throws LineUnavailableException, IOException {
        AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED, // encoding
                sampleRate,                      // sample rate
                sampleSizeInBits,                // sample size
                1,                               // channel: mono
                4,                               // frame size
                sampleRate,                      // frame rate
                isBigEndian                      // bigEndian
        );

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Line not supported");
            System.exit(0);
        }

        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bufferLengthInFrames = line.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * format.getFrameSize();

        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead;

        long endTime = System.currentTimeMillis() + 10000; // 현재 시간에서 10초 후
        while (System.currentTimeMillis() < endTime) {
            if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) break;
            out.write(data, 0, numBytesRead);
        }

        byte[] audioBytes = out.toByteArray();

        line.stop();
        line.close();
        out.close();
        return audioBytes;
    }
}
