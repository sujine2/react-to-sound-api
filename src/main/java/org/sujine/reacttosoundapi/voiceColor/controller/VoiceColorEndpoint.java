package org.sujine.reacttosoundapi.voiceColor.controller;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.sujine.reacttosoundapi.voiceColor.controller.formatter.RequestAudioStreamJSONDecoder;
import org.sujine.reacttosoundapi.voiceColor.controller.formatter.ResponseRGBJSONEncoder;
import org.sujine.reacttosoundapi.voiceColor.dto.RequestAudioStreamData;
import org.sujine.reacttosoundapi.voiceColor.service.VoiceService;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;

@ServerEndpoint(value = "/voiceColor",
                encoders = {ResponseRGBJSONEncoder.class},
                decoders = {RequestAudioStreamJSONDecoder.class}
)
public class VoiceColorEndpoint {
    private static Set<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        sessions.add(session);
        System.out.println("Client " + session.getId() + " opened");
        session.getBasicRemote().sendText("welcome ");
    }

    @OnMessage
    public void onMessage(Session session, RequestAudioStreamData message)
            throws IOException, IllegalArgumentException, ExecutionException, InterruptedException, EncodeException {
        session.getBasicRemote().sendObject(new VoiceService().getMainVoiceColor(message));
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
//            System.out.println(throwable.getMessage());
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

//    private static void broadcast(Message message) throws IOException, EncodeException {
//        sessions.forEach(session -> {
//            synchronized (session) {
//                try {
//                    sesasion.getBasicRemote().sendObject(message);
//                } catch (IOException | EncodeException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
}
