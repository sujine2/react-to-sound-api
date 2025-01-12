package org.sujine.reacttosoundapi.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.sujine.reacttosoundapi.utils.StreamDataFactory;
import org.sujine.reacttosoundapi.voiceColor.domain.VoiceStream;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;
import org.sujine.reacttosoundapi.voiceColor.service.VoiceColorExtractionService;
import org.sujine.reacttosoundapi.utils.AudioStreamFormatter;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.fail;

// sampleRate: [8000, 16000, 32000, 48000]
// sampleSize: [16, 32, 48]
@ExtendWith(MockitoExtension.class)
public class VoiceStreamServiceTests {
    private final VoiceColorExtractionService voiceColorExtractionService = new VoiceColorExtractionService();

    private StreamDataFactory streamDataFactory;

    @BeforeEach
    void setUp() {
        streamDataFactory = new StreamDataFactory();
    }

    @DisplayName("32000.0 sample rate audio stream")
    @Test
    public void getVad32SampleRateTest() throws IOException {
        streamDataFactory.setAudioFormat((float) 32000.0,16, false);

        RequestAudioStreamData request = streamDataFactory.createVoiceColorRequest();
        VoiceStream voiceStream = new VoiceStream(request.getRawStream(), request.getSampleRate());
        byte[] vadByte = AudioStreamFormatter.convertDoubleToByteArray(
                voiceStream.getStream(),
                request.getSampleSize(),
                request.isBigEndian()
        );
        streamDataFactory.createStreamFile(vadByte);
    }

    @DisplayName("48000.0 sample rate audio stream")
    @Test
    public void getVad48SampleRateTest() throws IOException {
        streamDataFactory.setAudioFormat((float) 48000.0,16, false);

        RequestAudioStreamData request = streamDataFactory.createVoiceColorRequest();
        VoiceStream voiceStream = new VoiceStream(request.getRawStream(), request.getSampleRate());
        byte[] vadByte = AudioStreamFormatter.convertDoubleToByteArray(
                voiceStream.getStream(),
                request.getSampleSize(),
                request.isBigEndian()
        );
        streamDataFactory.createStreamFile(vadByte);
    }

    @DisplayName("get color of 16000.0 sample rate audio stream")
    @Test
    public void getColor16SampleRateTest() throws IOException {
        streamDataFactory.setAudioFormat((float) 16000,16, false);
        RequestAudioStreamData request = streamDataFactory.createVoiceColorRequest();

        try {
            ResponseRGB[] colors = this.voiceColorExtractionService.getColorsWithThread(request);
            System.out.println(Arrays.deepToString(colors));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("getMainVoiceColor() failed");
        }
    }

    @DisplayName("get color of 32000.0 sample rate audio stream")
    @Test
    public void getColor32SampleRateTest() throws IOException {
        streamDataFactory.setAudioFormat((float) 32000,16, false);
        RequestAudioStreamData request = streamDataFactory.createVoiceColorRequest();

        try {
            ResponseRGB[] colors = this.voiceColorExtractionService.getColorsWithThread(request);
            System.out.println(Arrays.deepToString(colors));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("getMainVoiceColor() failed");
        }
    }

    @DisplayName("get color of 48000.0 sample rate audio stream")
    @Test
    public void getColor48SampleRateTest() throws IOException {
        streamDataFactory.setAudioFormat((float) 48000.0,16, false);
        RequestAudioStreamData request = streamDataFactory.createVoiceColorRequest();

        try {
            ResponseRGB[] colors = this.voiceColorExtractionService.getColorsWithThread(request);
            System.out.println(Arrays.deepToString(colors));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("getMainVoiceColor() failed");
        }
    }

    @DisplayName("get color function with multi thread")
    @Test
    public void getColorMultiThreadTest() throws IOException {
        streamDataFactory.setAudioFormat((float) 48000.0,16, false);
        RequestAudioStreamData request = streamDataFactory.createVoiceColorRequest();

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(VoiceColorExtractionService.class);
        VoiceColorExtractionService voiceService = context.getBean(VoiceColorExtractionService.class);
        try {
            for(int i=1; i<=10; i++) {
                ResponseRGB[] colors = voiceService.getColorsWithThread(request);
                System.out.println("multithread test # "+ i + " " + Arrays.deepToString(colors));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("getColorsWithThread() failed");
        }
        context.close();
        ExecutorService executor = this.voiceColorExtractionService.getExecutorService();
        System.out.println("multithread isTerminated # "+ executor.isTerminated());
    }

}