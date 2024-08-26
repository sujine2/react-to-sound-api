package org.sujine.reacttosoundapi.speechToText.controller;

import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.api.gax.rpc.ClientStream;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.sujine.reacttosoundapi.speechToText.controller.formatter.RequestAudioStreamJSONDecoder;
import org.sujine.reacttosoundapi.speechToText.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.speechToText.service.SpeechToTextService;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/speechToText",
        decoders = {RequestAudioStreamJSONDecoder.class}
)

public class SpeechToTextEndpoint {
    private static Set<Session> sessions = new CopyOnWriteArraySet<>();
    private SpeechToTextService speechToTextService = new SpeechToTextService();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        sessions.add(session);
        System.out.println("Client " + session.getId() + " opened");
        session.getBasicRemote().sendText("welcome ");
    }

    @OnMessage
    public void onMessage(Session session, RequestAudioStreamData message) throws Exception {
        SpeechToTextService.ResponseObserverSend responseObserver = new SpeechToTextService.ResponseObserverSend(session);
        ClientStream<StreamingRecognizeRequest> clientStream = speechToTextService.streamRecognize(message, responseObserver);
        speechToTextService.startSilenceTimer(session, clientStream);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        sessions.remove(session);
        System.out.println("Client " + session.getId() + " closed");
        session.getBasicRemote().sendText("bye" + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        try {
            if (session.isOpen()) {
                session.close(
                        new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Error occurred: " + throwable.getMessage())
                );
            }
        } catch (IOException e) {
            System.err.println("Failed to close session " + session.getId());
            e.printStackTrace();
        }
    }
}
