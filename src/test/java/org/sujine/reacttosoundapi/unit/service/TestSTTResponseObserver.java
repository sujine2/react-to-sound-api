package org.sujine.reacttosoundapi.unit.service;

import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.sujine.reacttosoundapi.ai.AIClient;
import org.sujine.reacttosoundapi.stt.dto.Text;
import org.sujine.reacttosoundapi.stt.service.STTResponseObserver;

@Service
@Profile("test")
public class TestSTTResponseObserver extends STTResponseObserver{
    private AIClient aiClient;
    @Autowired
    public TestSTTResponseObserver(AIClient aiClient) throws Exception {
        this.aiClient = aiClient;
    }

    @Override
    public void onStart(StreamController controller) {
        System.out.println("Streaming started.");
    }

    @Override
    public void onResponse(StreamingRecognizeResponse response) {
        try {
            if (!response.getResultsList().isEmpty()) {
                StreamingRecognitionResult result = response.getResultsList().get(0);
                String transcript = result.getAlternativesList().get(0).getTranscript();

                if (result.getIsFinal()) {
                    this.finalTranscript.append(transcript);
                    String answer = this.aiClient.generateResponse(transcript);
                    System.out.println("answer:" + new Text(answer, true));
                } else {
                    System.out.println(new Text(transcript, false).toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Throwable t) {
        String errorMessage = t.getMessage();
        System.err.println("Error during speech recognition: " + errorMessage);

        if (errorMessage != null && errorMessage.contains("OUT_OF_RANGE: Audio Timeout Error")) {
//                startSilenceTimer(this.clientStream);
        }
        t.printStackTrace();
    }

    @Override
    public void onComplete() {
        System.out.println("Speech recognition completed.");
    }


}
