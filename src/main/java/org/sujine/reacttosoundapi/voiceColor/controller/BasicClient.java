package org.sujine.reacttosoundapi.voiceColor.controller.base;

import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

@ClientEndpoint
public class BaseClient {
    Session session = null;
    private MessageHandler handler;

    public BaseClient(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        try {
            session.getBasicRemote().sendText("Opening connection");
        } catch (IOException e){
            System.out.println(e);
        }
    }

    public void addMessageHandler(MessageHandler msgHandler) {
        this.handler = msgHandler;
    }

    @OnMessage
    public void processMessage(String message) {
        System.out.println("Received message in client: " + message);
    }

    public void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException ex) {
            Logger.getLogger(BaseClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public static interface MessageHandler {

        public void handleMessage(String message);
    }
}
