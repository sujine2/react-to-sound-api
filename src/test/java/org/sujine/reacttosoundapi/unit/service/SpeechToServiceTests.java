package org.sujine.reacttosoundapi.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.sujine.reacttosoundapi.stt.service.STTStreamingService;
import org.sujine.reacttosoundapi.utils.StreamDataFactory;
import org.sujine.reacttosoundapi.stt.dto.SpeechAudioStream;


import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class SpeechToServiceTests {

    @InjectMocks
    private TestSTTResponseObserver sttResponseObserver;

    private StreamDataFactory streamDataFactory;

    @BeforeEach
    void setUp() {
        streamDataFactory = new StreamDataFactory();
    }

    @DisplayName("16000.0 sample rate speech to text")
    @Test
    public void STTTest() {
        streamDataFactory.setAudioFormat((float) 16000,16, false);
        SpeechAudioStream request = streamDataFactory.createSTTRequest();
        try {
            STTStreamingService STTService = new STTStreamingService(sttResponseObserver);
            STTService.initialize((int)request.getSampleRate());
            STTService.sendAudioData(request.getRawStream(), request.isLast());
            Thread.sleep(10000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("SpeechToTextService() failed");
        }
    }
}
