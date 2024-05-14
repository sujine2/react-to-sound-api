package org.example;


import jakarta.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import jakarta.websocket.EncodeException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/chatEndpoint/{username}")
//        encoders = {MessageEncoder.class},
//        decoders = {MessageDecoder.class})
public class ChatEndpoint {

    @Inject
    ChatSessionController chatSessionController;

    private static Session session;
    private static Set<Session> chatters = new CopyOnWriteArraySet<>();

    @OnOpen
    public void messageOpen(Session session,
                            @PathParam("username") String username) throws IOException,
            EncodeException {
        this.session = session;
        Map<String,String> chatusers = chatSessionController.getUsers();
        chatusers.put(session.getId(), username);
        chatSessionController.setUsers(chatusers);
        chatters.add(session);
        Message message = new Message();
        message.setUsername(username);
        message.setMessage("Welcome " + username);
        broadcast(message);
    }

    @OnMessage
    public void messageReceiver(Session session,
                                Message message) throws IOException, EncodeException {
        Map<String,String> chatusers = chatSessionController.getUsers();
        message.setUser(chatusers.get(session.getId()));
        broadcast(message);
    }

    @OnClose
    public void close(Session session) {
        chatters.remove(session);
        Message message = new Message();
        Map<String,String> chatusers = chatSessionController.getUsers();
        String chatuser = chatusers.get(session.getId());
        message.setUsername(chatuser);
        chatusers.remove(chatuser);
        message.setMessage("Disconnected from server");

    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("There has been an error with session " + session.getId());
    }

    private static void broadcast(Message message)
            throws IOException, EncodeException {

        chatters.forEach(session -> {
            synchronized (session) {
                try {
                    session.getBasicRemote().
                            sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}