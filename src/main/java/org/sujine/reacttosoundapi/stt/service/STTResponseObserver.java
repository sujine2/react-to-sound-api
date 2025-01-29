package org.sujine.reacttosoundapi.stt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.sujine.reacttosoundapi.stt.dto.Text;


@Service
@Scope("prototype")
@Setter
public class STTResponseObserver implements ResponseObserver<StreamingRecognizeResponse>{
    private WebSocketSession session;
    protected static final StringBuilder finalTranscript = new StringBuilder();


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
                    finalTranscript.append(transcript);
                    // send final question
                    sendMessage(session, new Text(transcript, true));
                } else {
                    // send intermediate transcript
                    sendMessage(session, new Text(transcript, false));
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

    private void sendMessage(WebSocketSession session, Text text) {
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(text);;
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }

}


