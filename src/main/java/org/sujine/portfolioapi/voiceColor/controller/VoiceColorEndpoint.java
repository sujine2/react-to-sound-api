package org.sujine.portfolioapi.voiceColor.controller;

import jakarta.websocket.EncodeException;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.ws.rs.PathParam;
import org.apache.logging.log4j.message.Message;
import org.sujine.portfolioapi.voiceColor.dto.RequestRawAudioStream;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/voiceColor/{clientId}")
public class VoiceColorEndpoint {
    private static Set<Session> sessions = new CopyOnWriteArraySet<>();
    private static Map<String, Session> requesters = null;

    @OnOpen
    public void onOpen(Session session, @PathParam("clientId") String clientId) throws IOException, EncodeException {
        requesters.put(clientId, session);
        sessions.add(session);
        sendString("welcome", clientId);
    }

    @OnMessage
    public void onMessage(Session session, RequestRawAudioStream message) throws IOException, EncodeException {

    }

    private static void sendString(String message, String clientId) throws IOException, EncodeException {
        try {
            requesters.get(clientId).getBasicRemote().sendObject(message);
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
        }
    }

//    private static void sendData(VoiceStream stream, String clientId) throws IOException, EncodeException {
//        try {
//            requesters.get(clientId).getBasicRemote().
//                    sendObject(message);
//        } catch (IOException | EncodeException e) {
//            e.printStackTrace();
//        }
//    }

    private static void broadcast(Message message) throws IOException, EncodeException {
        sessions.forEach(session -> {
            synchronized (session) {
                try {
                    session.getBasicRemote().sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
