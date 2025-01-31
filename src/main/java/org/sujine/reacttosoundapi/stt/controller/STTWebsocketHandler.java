package org.sujine.reacttosoundapi.stt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.WebSocketMessage;
import org.sujine.reacttosoundapi.stt.dto.SpeechAudioStream;
import org.sujine.reacttosoundapi.stt.service.STTResponseObserver;
import org.sujine.reacttosoundapi.stt.service.STTStreamingService;
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
class STTWebsocketHandler extends TextWebSocketHandler {

    private final ObjectProvider<STTResponseObserver> responseObserverProvider;
    private final ObjectProvider<STTStreamingService> streamingServiceProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<WebSocketSession, STTStreamingService> sessionServiceMap = new ConcurrentHashMap<>();

    public STTWebsocketHandler(ObjectProvider<STTResponseObserver> responseObserverProvider,
                               ObjectProvider<STTStreamingService> streamingServiceProvider) {
        this.responseObserverProvider = responseObserverProvider;
        this.streamingServiceProvider = streamingServiceProvider;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) {
            session.close();
            return;
        }

        session.setTextMessageSizeLimit(1024 * 1024); // 1MB
        session.setBinaryMessageSizeLimit(1024 * 1024); // 1MB
        session.sendMessage(new TextMessage("websocket connection established"));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (!session.isOpen()) {
            System.out.println("🚨 session is closed");
            return;
        }

        SpeechAudioStream audioStream = objectMapper.readValue(
                (String)message.getPayload(),
                SpeechAudioStream.class
        );

        STTStreamingService sttService = sessionServiceMap.get(session);
        if (sttService == null) {
            STTResponseObserver responseObserver = this.responseObserverProvider.getObject();
            responseObserver.setSession(session);
            sttService = this.streamingServiceProvider.getObject(responseObserver);
            sttService.initialize((int) audioStream.getSampleRate());

            sessionServiceMap.put(session, sttService);
        }
        sttService.sendAudioData(audioStream.getRawStream(), audioStream.isLast());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        STTStreamingService sttService = sessionServiceMap.get(session);
        if (sttService != null) {sttService.closeStreaming();}
        sessionServiceMap.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws IOException {
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }
}
