package org.sujine.reacttosoundapi.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sujine.reacttosoundapi.TestStreamData;
import org.sujine.reacttosoundapi.qna.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.TestSTTService;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class
SpeechToServiceTests {

    @DisplayName("16000.0 sample rate speech to text")
    @Test
    public void getText() throws IOException {
        TestStreamData generator = new TestStreamData((float) 16000,16, false);
        RequestAudioStreamData request = this.createRequest(generator, "16");

        try {
            TestSTTService.ResponseObserverNotSend responseObserver = new TestSTTService.ResponseObserverNotSend();
            TestSTTService STTService = new TestSTTService((int)request.getSampleRate(), responseObserver);
            STTService.sendAudioData(request.getRawStream(), request.isFinal());
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

//            double[] stream = AudioStreamFormatter.convertStreamToDoubleArray(
//                    inputRawByte,
//                    16,
//                    false
//            );
            request = new RequestAudioStreamData(
                    inputRawByte,
                    inputStream.getFormat().getSampleRate(),
                    inputStream.getFormat().getSampleSizeInBits(),
                    1,
                    false,
                    true
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("createRawVoiceStream() failed");
        }
        return request;
    }

}
