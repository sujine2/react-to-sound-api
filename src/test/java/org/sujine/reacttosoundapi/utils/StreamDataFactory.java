package org.sujine.reacttosoundapi.utils;

import org.sujine.reacttosoundapi.voice.dto.AudioStreamData;
import org.sujine.reacttosoundapi.stt.dto.SpeechAudioStream;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class StreamDataFactory {
    AudioFormat audioFormat;
    private static final int channel = 1; // mono

    public void setAudioFormat(float sampleRate, int sampleSize, boolean bigEndian) {
         this.audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,    // encoding
                 sampleRate,                        // sample rate
                 sampleSize,                        // sample size
                 this.channel,                       // channel: mono
                 (sampleSize / 8) * this.channel,   // frame size
                 sampleRate,                        // frame rate
                 bigEndian                          // bigEndian
        );
    }

    public void createStreamFile(byte[] outputStream) {
        try {
            AudioInputStream vadStream = this.createAudioInputStream(outputStream);
            File file = new File("vad"+ this.audioFormat.getSampleRate() + "TestResult.wav");
            AudioSystem.write(vadStream, AudioFileFormat.Type.WAVE, file);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("createVoiceStream() failed");
        }
    }

    public SpeechAudioStream createSTTRequest() {
        SpeechAudioStream request = null;
        try {
            byte[] inputRawByte = this.createVoiceRawStream();
            AudioInputStream inputStream = this.createAudioInputStream(inputRawByte);
            File file = new File("vad" + this.audioFormat.getSampleRate() + "TestInput.wav");
            AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, file);

//            double[] stream = AudioStreamFormatter.convertStreamToDoubleArray(
//                    inputRawByte,
//                    16,
//                    false
//            );
            request = new SpeechAudioStream(
                    inputRawByte,
                    inputStream.getFormat().getSampleRate(),
                    inputStream.getFormat().getSampleSizeInBits(),
                    1,
                    false
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("createRawVoiceStream() failed");
        }
        return request;
    }

    public AudioStreamData createVoiceColorRequest() {
        AudioStreamData request = null;
        try {
            byte[] inputRawByte = this.createVoiceRawStream();
            AudioInputStream inputStream = this.createAudioInputStream(inputRawByte);
            File file = new File("vad" + this.audioFormat.getSampleRate() + "TestInput.wav");
            AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, file);

            double[] stream = AudioStreamFormatter.convertStreamToDoubleArray(
                    inputRawByte,
                    16,
                    false
            );
            request = new AudioStreamData(
                    stream,
                    inputStream.getFormat().getSampleRate(),
                    inputStream.getFormat().getSampleSizeInBits(),
                    1,
                    false
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("createRawVoiceStream() failed");
        }
        return request;
    }

    private byte[] createVoiceRawStream() throws LineUnavailableException, IOException {
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

        line.stop();
        line.close();
        out.close();
        return out.toByteArray();
    }

    private AudioInputStream createAudioInputStream(byte[] rawAudioBytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(rawAudioBytes);
        AudioInputStream audioStream = new AudioInputStream(bais, this.audioFormat, rawAudioBytes.length / this.audioFormat.getFrameSize());
        long milliseconds = (long) ((audioStream.getFrameLength() * 1000) / this.audioFormat.getFrameRate());
        double duration = milliseconds / 1000.0;
        System.out.println("Audio duration in seconds: " + duration);

        return audioStream;
    }

    private void buildByteOutputStream(final ByteArrayOutputStream out, final TargetDataLine line, final int bufferLengthInBytes) {
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead;

        long endTime = System.currentTimeMillis() + 5000; // 5s
        while (System.currentTimeMillis() < endTime) {
            if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) break;
            out.write(data, 0, numBytesRead);
        }
    }
}
