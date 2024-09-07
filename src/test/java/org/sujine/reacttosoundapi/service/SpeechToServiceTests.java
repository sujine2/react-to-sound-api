package org.sujine.reacttosoundapi.service;

import com.google.api.gax.rpc.ClientStream;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sujine.reacttosoundapi.TestStreamData;
import org.sujine.reacttosoundapi.speechToText.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.speechToText.service.SpeechToTextService;
import org.sujine.reacttosoundapi.voiceColor.utils.AudioStreamFormatter;

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

        try {
            SpeechToTextService speechToTextService = new SpeechToTextService();
            SpeechToTextService.ResponseObserverNotSend responseObserver = new SpeechToTextService.ResponseObserverNotSend();

            speechToTextService.initialize((int)request.getSampleRate(), responseObserver);
            speechToTextService.sendAudioData(request.getRawStream(), request.isFinal());
            Thread.sleep(1000);
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
