package org.sujine.reacttosoundapi.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sujine.reacttosoundapi.TestHelper;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;
import org.sujine.reacttosoundapi.voiceColor.service.VoiceService;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.fail;

public class VoiceServiceTests {
    // sampleRate: [8000, 16000, 32000, 48000]
    // sampleSize: [16, 32, 48]
    @DisplayName("32000.0 sample rate audio stream")
    @Test
    public void getVad32SampleRateTest() throws IOException {
        TestHelper helper = new TestHelper((float) 32000.0,16, false);
        byte[] inputRawByte = null;
        try {
            inputRawByte = helper.createVoiceRawStream();
            AudioInputStream inputStream = helper.createAudioInputStream(inputRawByte);
            File file = new File("vad16TestInput.wav");
            AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, file);

            RequestAudioStreamData request = new RequestAudioStreamData(
                    inputRawByte,
                    inputStream.getFormat().getSampleRate(),
                    inputStream.getFormat().getSampleSizeInBits(),
                    1,
                    false
            );
            byte[] vadByte = VoiceService.getVad(request);
            AudioInputStream vadStream = helper.createAudioInputStream(vadByte);
            file = new File("vad16TestResult.wav");
            AudioSystem.write(vadStream, AudioFileFormat.Type.WAVE, file);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("No exception should be thrown");
        }
    }

    @DisplayName("48000.0 sample rate audio stream")
    @Test
    public void getVad48SampleRateTest() throws IOException {
        TestHelper helper = new TestHelper((float) 48000.0,16, false);
        byte[] inputRawByte = null;
        try {
            inputRawByte = helper.createVoiceRawStream();
            AudioInputStream inputStream = helper.createAudioInputStream(inputRawByte);
            File file = new File("vad24TestInput.wav");
            AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, file);

            RequestAudioStreamData request = new RequestAudioStreamData(
                    inputRawByte,
                    inputStream.getFormat().getSampleRate(),
                    inputStream.getFormat().getSampleSizeInBits(),
                    1,
                    false
            );
            byte[] vadByte = VoiceService.getVad(request);
            AudioInputStream vadStream = helper.createAudioInputStream(vadByte);
            file = new File("vad24TestResult.wav");
            AudioSystem.write(vadStream, AudioFileFormat.Type.WAVE, file);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("No exception should be thrown");
        }
    }

    @DisplayName("get color of 48000.0 sample rate audio stream")
    @Test
    public void getColor48SampleRateTest() throws IOException {
        TestHelper helper = new TestHelper((float) 48000.0,16, false);
        byte[] inputRawByte = null;
        try {
            inputRawByte = helper.createVoiceRawStream();
            AudioInputStream inputStream = helper.createAudioInputStream(inputRawByte);
            RequestAudioStreamData request = new RequestAudioStreamData(
                    inputRawByte,
                    inputStream.getFormat().getSampleRate(),
                    inputStream.getFormat().getSampleSizeInBits(),
                    1,
                    false
            );
            ResponseRGB[] colors = VoiceService.getMainVoiceColor(request);
            System.out.println(Arrays.deepToString(colors));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("No exception should be thrown");
        }
    }
}
