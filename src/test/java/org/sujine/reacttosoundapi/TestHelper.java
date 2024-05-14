package org.sujine.reacttosoundapi;

import lombok.AllArgsConstructor;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@AllArgsConstructor
public class TestHelper {
    AudioFormat audioFormat;

    public float sampleRate;
    public int sampleSize;
    public float frameRate;
    public int frameSize;
    public int channel = 1; // mono
    public boolean bigEndian;


    public TestHelper(float sampleRate, int sampleSize, boolean bigEndian) {
        this.sampleSize = sampleSize;
        this.sampleRate = sampleRate;
        this.frameSize = (sampleSize / 8) * this.channel;
        this.frameRate = sampleRate;
        this.bigEndian = bigEndian;

         this.audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED, // encoding
                this.sampleRate,                 // sample rate
                this.sampleSize,                 // sample size
                this.channel,                    // channel: mono
                this.frameSize,                  // frame size
                this.frameRate,                  // frame rate
                this.bigEndian                   // bigEndian
        );

    }
    public byte[] createVoiceRawStream() throws LineUnavailableException, IOException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, this.audioFormat);
        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Line not supported");
            System.exit(0);
        }

        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info); // read all the incoming data into an audio stream
        line.open(this.audioFormat); line.start();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bufferLengthInFrames = line.getBufferSize() / 8;
        buildByteOutputStream(out, line, bufferLengthInFrames * this.audioFormat.getFrameSize());


        line.stop(); line.close();
        out.close();
        return out.toByteArray();
    }

    public AudioInputStream createAudioInputStream(byte[] audioBytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
        AudioInputStream audioStream = new AudioInputStream(bais, this.audioFormat, audioBytes.length / this.frameSize);
        long milliseconds = (long) ((audioStream.getFrameLength() * 1000) / this.audioFormat.getFrameRate());
        double duration = milliseconds / 1000.0;
        System.out.println("Audio duration in seconds: " + duration);

        return audioStream;
    }

    private void buildByteOutputStream(final ByteArrayOutputStream out, final TargetDataLine line, final int bufferLengthInBytes) {
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead;

        long endTime = System.currentTimeMillis() + 10000; // 10s
        while (System.currentTimeMillis() < endTime) {
            if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) break;
            out.write(data, 0, numBytesRead);
        }
    }
}
