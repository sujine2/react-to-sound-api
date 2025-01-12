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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.sujine.reacttosoundapi.qna.domain.Qna;
import org.sujine.reacttosoundapi.qna.dto.Response;
import org.sujine.reacttosoundapi.qna.repository.QnaRepository;

@Service
@Scope("prototype")
@Setter
public class STTResponseObserver implements ResponseObserver<StreamingRecognizeResponse>{
    private WebSocketSession session;
    protected final QnaService qnaService;
    protected static final StringBuilder finalTranscript = new StringBuilder();

    @Autowired
    public STTResponseObserver(QnaService qnaService) {
        this.qnaService = qnaService;
    }

    @Override
    public void onStart(StreamController controller) {
        System.out.println("Streaming started.");
    }

    @Transactional
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
                    sendMessage(session, new Response(transcript, false, true));
                    // call OpenAI API
                    String answer = this.qnaService.requestOpenAI(transcript);
                    // System.out.println(answer);
                    // save QnA
                    this.qnaService.saveQna(transcript, answer, (String) session.getAttributes().get("userId"));

                    // send answer
                    sendMessage(session, new Response(answer, true, false));
                } else {
                    // send intermediate transcript
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


