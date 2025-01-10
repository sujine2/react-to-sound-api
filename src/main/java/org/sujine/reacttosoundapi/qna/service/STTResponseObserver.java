package org.sujine.reacttosoundapi.qna.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.sujine.reacttosoundapi.qna.dto.Response;

@Service
@Scope("prototype")
@Setter
public class STTResponseObserver implements ResponseObserver<StreamingRecognizeResponse>{
    private WebSocketSession session;
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
                    // Append final transcript
                    this.finalTranscript.append(transcript);

                    // Send question to client
                    sendMessage(session, new Response(transcript, false, true));

                    // Generate and send answer using OpenAI
                    String answer = this.openAIService.askGpt(transcript);
                    sendMessage(session, new Response(answer, true, false));
                } else {
                    // Send intermediate transcript
                    sendMessage(session, new Response(transcript, false, false));
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

    private void sendMessage(WebSocketSession session, Response response) {
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(response);;
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


