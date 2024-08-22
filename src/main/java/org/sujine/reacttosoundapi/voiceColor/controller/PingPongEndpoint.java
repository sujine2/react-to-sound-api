package org.sujine.reacttosoundapi.voiceColor.controller;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/ping")
public class PingPongEndpoint {

    private Session session;
    private ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        timer.scheduleAtFixedRate(() -> {
            sendPingMessage();
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void sendPingMessage() {
        try {
            ByteBuffer pingData = ByteBuffer.wrap("Ping".getBytes());
            this.session.getBasicRemote().sendPing(pingData);
            System.out.println("Sent ping to " + session.getId());
        } catch (IOException e) {
            System.out.println("Ping failed: " + e.getMessage());
            timer.shutdown();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Message received: " + message);
        if ("stop".equalsIgnoreCase(message)) {
            timer.shutdown();
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Session " +session.getId()+ " has ended");
        timer.shutdown();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("Error: " + throwable.getMessage());
    }
}

