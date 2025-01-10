package org.sujine.reacttosoundapi;

import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import org.springframework.stereotype.Service;
import org.sujine.reacttosoundapi.qna.dto.Answer;
import org.sujine.reacttosoundapi.qna.service.OpenAIService;
import org.sujine.reacttosoundapi.qna.service.STTResponseObserver;

@Service
public class TestSTTResponseObserver extends STTResponseObserver{ ;
    public TestSTTResponseObserver(OpenAIService openAIService) throws Exception{
        super(openAIService);
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
                    System.out.println(new Answer(transcript, true,true).toString());
                    String answer = this.openAIService.askGpt(transcript);
                    System.out.println("answer:" + new Answer(answer, true, false));
                } else {
                    System.out.println(new Answer(transcript, true,false).toString());
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
