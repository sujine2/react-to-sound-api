package org.sujine.reacttosoundapi.service;

import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.sujine.reacttosoundapi.qna.dto.Response;
import org.sujine.reacttosoundapi.qna.service.QnaService;
import org.sujine.reacttosoundapi.qna.service.STTResponseObserver;

@Service
@Profile("test")
public class TestSTTResponseObserver extends STTResponseObserver{
    @Autowired
    public TestSTTResponseObserver(QnaService qnaService) throws Exception {
        super(qnaService);
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
                    String answer = this.qnaService.requestOpenAI(transcript);
                    System.out.println("answer:" + new Response(answer, true, false));
                } else {
                    System.out.println(new Response(transcript, true,false).toString());
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
