package org.sujine.reacttosoundapi.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sujine.reacttosoundapi.TestStreamData;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;
import org.sujine.reacttosoundapi.voiceColor.service.VoiceService;
import org.sujine.reacttosoundapi.voiceColor.utils.AudioStreamFormatter;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.fail;

public class VoiceServiceTests {

    // sampleRate: [8000, 16000, 32000, 48000]
    // sampleSize: [16, 32, 48]
    @DisplayName("32000.0 sample rate audio stream")
    @Test
    public void getVad32SampleRateTest() throws IOException {
        TestStreamData generator = new TestStreamData((float) 32000.0,16, false);

        RequestAudioStreamData request = this.createRequest(generator, "16");
        byte[] vadByte = VoiceService.getVad(request);
        this.createVoiceStream(generator, vadByte,"16");
    }

    @DisplayName("48000.0 sample rate audio stream")
    @Test
    public void getVad48SampleRateTest() throws IOException {
        TestStreamData generator = new TestStreamData((float) 48000.0,16, false);

        RequestAudioStreamData request = this.createRequest(generator, "48");
        byte[] vadByte = VoiceService.getVad(request);
        this.createVoiceStream(generator, vadByte,"48");
    }

    @DisplayName("get color of 16000.0 sample rate audio stream")
    @Test
    public void getColor16SampleRateTest() throws IOException {
        TestStreamData generator = new TestStreamData((float) 16000,16, false);
        RequestAudioStreamData request = this.createRequest(generator, "16");
        try {
            ResponseRGB[] colors = new VoiceService().getMainVoiceColor(request);
            System.out.println(Arrays.deepToString(colors));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("getMainVoiceColor() failed");
        }
    }

    @DisplayName("get color of 32000.0 sample rate audio stream")
    @Test
    public void getColor32SampleRateTest() throws IOException {
        TestStreamData generator = new TestStreamData((float) 32000,16, false);
        RequestAudioStreamData request = this.createRequest(generator, "32");
        try {
            ResponseRGB[] colors = new VoiceService().getMainVoiceColor(request);
            System.out.println(Arrays.deepToString(colors));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("getMainVoiceColor() failed");
        }
    }

    @DisplayName("get color of 48000.0 sample rate audio stream")
    @Test
    public void getColor48SampleRateTest() throws IOException {
        TestStreamData generator = new TestStreamData((float) 48000.0,16, false);
        RequestAudioStreamData request = this.createRequest(generator, "48");
        try {
            ResponseRGB[] colors = new VoiceService().getMainVoiceColor(request);
            System.out.println(Arrays.deepToString(colors));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("getMainVoiceColor() failed");
        }
    }

    @DisplayName("get color function with multi thread")
    @Test
    public void getColorMultiThreadTest() throws IOException {
        TestStreamData generator = new TestStreamData((float) 48000.0,16, false);
        RequestAudioStreamData request = this.createRequest(generator, "48");
        VoiceService voiceService = new VoiceService();
        try {
            for(int i=1; i<=10; i++) {
                ResponseRGB[] colors = voiceService.getMainVoiceColor(request);
                System.out.println("multithread test # "+ i + " " +Arrays.deepToString(colors));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("getMainVoiceColor() failed");
        }

        ArrayList<ExecutorService> executors = voiceService.getExecutors();
        for(ExecutorService executor : executors) {
            System.out.println("multithread test # "+ executor.isTerminated());
        }
    }

    private RequestAudioStreamData createRequest(TestStreamData generator, String sampleRate) {
        RequestAudioStreamData request = null;
        try {
            byte[] inputRawByte = generator.createVoiceRawStream();
            AudioInputStream inputStream = generator.createAudioInputStream(inputRawByte);
            File file = new File("vad"+sampleRate+"TestInput.wav");
            AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, file);

            double[] stream = AudioStreamFormatter.convertStreamToDoubleArray(
                    inputRawByte,
                    16,
                    false
            );
            request = new RequestAudioStreamData(
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

    private void createVoiceStream(TestStreamData generator, byte[] outputStream, String sampleRate) {
        try {
            AudioInputStream vadStream = generator.createAudioInputStream(outputStream);
            File file = new File("vad16TestResult.wav");
            AudioSystem.write(vadStream, AudioFileFormat.Type.WAVE, file);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("createVoiceStream() failed");
        }
    }

}
