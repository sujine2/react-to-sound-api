package org.sujine.reacttosoundapi.audio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.WebSocketMessage;
import org.sujine.reacttosoundapi.audio.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.audio.dto.ResponseRGB;
import org.sujine.reacttosoundapi.audio.service.VoiceColorExtractionService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ConcurrentMap;

@Component
class VoiceColorWebSocketHandler extends TextWebSocketHandler {

    private final VoiceColorExtractionService voiceColorExtractionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentMap<WebSocketSession, Boolean> sessions = new ConcurrentHashMap<>();

    VoiceColorWebSocketHandler(VoiceColorExtractionService voiceColorExtractionService) {
        this.voiceColorExtractionService = voiceColorExtractionService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        sessions.put(session, true);
        session.sendMessage(new TextMessage("websocket connection established"));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            RequestAudioStreamData requestStream = objectMapper.readValue((String)message.getPayload(), RequestAudioStreamData.class);

            // Process the request and send the response
            ResponseRGB[] responseRGBs = voiceColorExtractionService.getColorsWithThread(requestStream);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseRGBs)));

        } catch (IllegalArgumentException | ExecutionException | InterruptedException e) {
            session.sendMessage(new TextMessage("Error: " + e.getMessage()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws IOException {
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

}
