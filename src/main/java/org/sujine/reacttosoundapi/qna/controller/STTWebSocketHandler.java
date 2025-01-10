package org.sujine.reacttosoundapi.qna.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.WebSocketMessage;
import org.sujine.reacttosoundapi.qna.dto.QuestionAudioStream;
import org.sujine.reacttosoundapi.qna.service.STTResponseObserver;
import org.sujine.reacttosoundapi.qna.service.STTStreamingService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class STTWebSocketHandler extends TextWebSocketHandler {

    private final ObjectProvider<STTResponseObserver> responseObserverProvider;
    private final ObjectProvider<STTStreamingService> streamingServiceProvider;
    private  final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<WebSocketSession, STTStreamingService> sessionServiceMap = new ConcurrentHashMap<>();

    public STTWebSocketHandler(ObjectProvider<STTResponseObserver> responseObserverProvider,
                               ObjectProvider<STTStreamingService> streamingServiceProvider) {
        this.responseObserverProvider = responseObserverProvider;
        this.streamingServiceProvider = streamingServiceProvider;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        session.setTextMessageSizeLimit(1024 * 1024); // 1MB
        session.setBinaryMessageSizeLimit(1024 * 1024); // 1MB
        session.sendMessage(new TextMessage("start"));
        System.out.println("Connection established: " + session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        QuestionAudioStream audioStream = objectMapper.readValue((String)message.getPayload(), QuestionAudioStream.class);

        STTStreamingService sttService = sessionServiceMap.get(session);
        if (sttService == null) {
            STTResponseObserver responseObserver = this.responseObserverProvider.getObject();
            responseObserver.setSession(session);
            sttService = this.streamingServiceProvider.getObject(responseObserver);
            sttService.initialize((int) audioStream.getSampleRate());

            sessionServiceMap.put(session, sttService);
        }

        sttService.sendAudioData(audioStream.getRawStream(), audioStream.isFinal());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        sessionServiceMap.remove(session);
        System.out.println("Connection closed: " + session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws IOException {
        System.err.println("Error on session " + session.getId() + ": " + exception.getMessage());
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }
}
