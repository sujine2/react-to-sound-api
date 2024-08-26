package org.sujine.reacttosoundapi.service;

import com.google.api.gax.rpc.ClientStream;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sujine.reacttosoundapi.TestStreamData;
import org.sujine.reacttosoundapi.speechToText.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.speechToText.service.SpeechToTextService;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.fail;

public class
SpeechToServiceTests {

    @DisplayName("16000.0 sample rate speech to text")
    @Test
    public void getText() throws IOException {
        TestStreamData generator = new TestStreamData((float) 16000,16, false);
        RequestAudioStreamData request = this.createRequest(generator, "16");

        SpeechToTextService speechToTextService = new SpeechToTextService();
        try {
            SpeechToTextService.ResponseObserverNotSend responseObserver = new SpeechToTextService.ResponseObserverNotSend();
            ClientStream<StreamingRecognizeRequest> clientStream = speechToTextService.streamRecognize(request, responseObserver);
            speechToTextService.startSilenceTimer(clientStream);
            Thread.sleep(10000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("SpeechToTextService() failed");
        }
    }

    private RequestAudioStreamData createRequest(TestStreamData generator, String sampleRate) {
        RequestAudioStreamData request = null;
        try {
            byte[] inputRawByte = generator.createVoiceRawStream();
            AudioInputStream inputStream = generator.createAudioInputStream(inputRawByte);
            File file = new File("vad"+sampleRate+"TestInput.wav");
            AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, file);

            request = new RequestAudioStreamData(
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

}
