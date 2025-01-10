package org.sujine.reacttosoundapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.sujine.reacttosoundapi.qna.dto.QuestionAudioStream;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class TestWebSocketClient extends TextWebSocketHandler {
    public static CompletableFuture<String> messageFuture = new CompletableFuture<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebSocketSession session;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;
        System.out.println("Connected to server.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        messageFuture.complete(message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        messageFuture.completeExceptionally(exception);
    }

    public void sendAudioStream(QuestionAudioStream message) throws Exception {
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } else {
            throw new IOException("WebSocket session is not open.");
        }
    }
}
