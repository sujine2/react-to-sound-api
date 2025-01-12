package org.sujine.reacttosoundapi.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.sujine.reacttosoundapi.qna.dto.QuestionAudioStream;
import org.sujine.reacttosoundapi.qna.dto.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class TestWebSocketClientHandler extends TextWebSocketHandler {
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
        String payload = message.getPayload();
        System.out.println("Received Response: " + payload);
        messageFuture.complete(payload);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        messageFuture.completeExceptionally(exception);
    }

    public void sendAudioStream(QuestionAudioStream message) throws Exception {
        if (this.session != null && this.session.isOpen()) {
            this.session.sendMessage(new TextMessage(this.objectMapper.writeValueAsString(message)));
        } else {
            throw new IOException("WebSocket session is not open.");
        }
    }
}
