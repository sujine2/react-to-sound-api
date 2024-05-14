package org.sujine.reacttosoundapi.voiceColor.controller;

import jakarta.websocket.*;
import org.sujine.reacttosoundapi.voiceColor.controller.formatter.ResponseRGBJOSNDecoder;
import org.sujine.reacttosoundapi.voiceColor.dto.ResponseRGB;

import java.io.IOException;
import java.net.URI;
import java.nio.Buffer;
import java.nio.ByteBuffer;

@ClientEndpoint(decoders = {ResponseRGBJOSNDecoder.class})
public class BasicClient {
    Session session = null;
    private MessageHandler handler;

    public BasicClient(URI endpointURI) {
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

    public void sendAudiStream(byte[] stream) throws IOException{
        session.getBasicRemote().sendBinary(ByteBuffer.wrap(stream));
    }

    @OnMessage
    public void onMessage(Session session, ResponseRGB[] msg) {
        System.out.println(msg.toString());
    }

    @OnMessage
    public void onMessage(Session session, byte[] msg) {
        System.out.println(msg);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
    }

    public void Disconnect() throws IOException {
        session.close();
    }

}
