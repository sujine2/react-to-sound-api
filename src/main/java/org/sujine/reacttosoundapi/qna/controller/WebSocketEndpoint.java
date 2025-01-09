package org.sujine.reacttosoundapi.qna.controller;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.ObjectProvider;
import org.sujine.reacttosoundapi.qna.controller.formatter.RequestAudioStreamJSONDecoder;
import org.sujine.reacttosoundapi.qna.controller.formatter.ResponseAudioTextEncoder;
import org.sujine.reacttosoundapi.qna.dto.QuestionAudioStream;
import org.sujine.reacttosoundapi.qna.service.STTResponseObserver;
import org.sujine.reacttosoundapi.qna.service.STTStreamingService;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@ServerEndpoint(value = "/speechToText",
        encoders = {ResponseAudioTextEncoder.class},
        decoders = {RequestAudioStreamJSONDecoder.class}
)

public class WebSocketEndpoint {
    private static Set<Session> sessions = new CopyOnWriteArraySet<>();
    private static Map<Session, STTStreamingService> sessionServiceMap = new ConcurrentHashMap<>();
    private final ObjectProvider<STTResponseObserver> responseObserverProvider;
    private final ObjectProvider<STTStreamingService> streamingServiceProvider;

    public WebSocketEndpoint(ObjectProvider<STTResponseObserver> responseObserverProvider, ObjectProvider<STTStreamingService> streamingServiceProvider) {
        this.responseObserverProvider = responseObserverProvider;
        this.streamingServiceProvider = streamingServiceProvider;
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        sessions.add(session);
        session.setMaxTextMessageBufferSize(1024 * 1024); // 1MB
        session.setMaxBinaryMessageBufferSize(1024 * 1024); // 1MB
//        System.out.println("Client " + session.getId() + " opened");
        session.getBasicRemote().sendText("start");
    }

    @OnMessage
    public void onMessage(Session session, QuestionAudioStream message) throws Exception {
        STTStreamingService speechService = sessionServiceMap.get(session);
        if (speechService == null) {
            STTResponseObserver responseObserver = this.responseObserverProvider.getObject();
            responseObserver.setSession(session);
            STTStreamingService STTService = this.streamingServiceProvider.getObject(responseObserver);
            STTService.initialize((int)message.getSampleRate());

            sessionServiceMap.put(session, STTService);
            STTService.sendAudioData(message.getRawStream(), message.isFinal());
        } else {
            speechService.sendAudioData(message.getRawStream(), message.isFinal());
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        sessionServiceMap.remove(session);
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