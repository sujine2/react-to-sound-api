package org.sujine.reacttosoundapi.qna.service;

import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import jakarta.websocket.Session;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.sujine.reacttosoundapi.qna.dto.Answer;

@Service
@Scope("prototype")
@Setter
public class STTResponseObserver implements ResponseObserver<StreamingRecognizeResponse>{
    private Session session;
    protected final OpenAIService openAIService;
    protected static final StringBuilder finalTranscript = new StringBuilder();

    @Autowired
    public STTResponseObserver(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @Override
    public void onStart(StreamController controller) {
        System.out.println("Streaming started.");
    }

    @Override
    public void onResponse(StreamingRecognizeResponse response) {
        try {
//                System.out.println("Streaming response received.");
            if (!response.getResultsList().isEmpty()) {
                StreamingRecognitionResult result = response.getResultsList().get(0);
                String transcript = result.getAlternativesList().get(0).getTranscript();

                if (result.getIsFinal()) {
                    // question
                    this.finalTranscript.append(transcript);
                    this.session.getBasicRemote().sendObject(new Answer(transcript, false, true));
                    // answer
                    String answer = this.openAIService.askGpt(transcript);
                    this.session.getBasicRemote().sendObject(new Answer(answer, true, false));
                } else {
                    this.session.getBasicRemote().sendObject(new Answer(transcript, false,false));
                }
            }
        } catch (Exception e) {
            System.err.println("Error during speech response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Throwable t) {
        String errorMessage = t.getMessage();
        System.err.println("Error during speech recognition: " + errorMessage);

        t.printStackTrace();
    }

    @Override
    public void onComplete() {
        System.out.println("Speech recognition completed.");
    }

    public String getFinalTranscript() {
        return finalTranscript.toString();
    }
}


