package org.sujine.reacttosoundapi.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.sujine.reacttosoundapi.qna.dto.QuestionAudioStream;
import org.sujine.reacttosoundapi.qna.service.OpenAIService;
import org.sujine.reacttosoundapi.utils.StreamDataFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class STTWebsocketEndpointTests {

    @Autowired
    private OpenAIService openAIService;
    private static final List<WebSocketSession> webSocketSessions = new ArrayList<>();
    private static final List<TestWebSocketClientHandler> handlers = new ArrayList<>();
    private static final String WS_URI = "ws://localhost:%d/speechToText";
    private StreamDataFactory streamDataFactory;

    @BeforeEach
    void setUp() {
        streamDataFactory = new StreamDataFactory();
    }

    @BeforeAll
    static void setUp(@LocalServerPort int port) throws ExecutionException, InterruptedException {
        String uri = String.format(WS_URI, port);
        StandardWebSocketClient client = new StandardWebSocketClient();

        // client1
        handlers.add(new TestWebSocketClientHandler());
        webSocketSessions.add(client.execute(handlers.get(0), uri).get());

        // client2
        handlers.add(new TestWebSocketClientHandler());
        webSocketSessions.add(client.execute(handlers.get(1),uri).get());

        // client3
        handlers.add(new TestWebSocketClientHandler());
        webSocketSessions.add(client.execute(handlers.get(2), uri).get());
    }

    @AfterAll
    static void teardown() throws Exception {
        for (WebSocketSession session : webSocketSessions) {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        webSocketSessions.clear();
    }

    @Test
    void testOpenAIObject() {
        System.out.println("Default Charset: " + java.nio.charset.Charset.defaultCharset());
        System.out.println("info: " + openAIService.getPersonaInfo());
    }

    @DisplayName("A client sends a request to the STT endpoint.")
    @Test
    void STTWebsocketEndpointTest() {
        streamDataFactory.setAudioFormat((float) 16000, 16, false);
        QuestionAudioStream request = streamDataFactory.createSTTRequest();
        try {
            handlers.get(1).sendAudioStream(request);
            String receivedMessage = TestWebSocketClientHandler.messageFuture.get(); // Blocking
            System.out.println("Received message: " + receivedMessage);
            Thread.sleep(10000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("SpeechToTextService() failed");
        }
    }

    @DisplayName("Clients send requests to the STT endpoint.")
    @Test
    void STTWebsocketEndpointWithClientsTest() {
        try {
            for (int i = 0; i < handlers.size(); i++) {
                streamDataFactory.setAudioFormat((float) 16000, 16, false);
                QuestionAudioStream request = streamDataFactory.createSTTRequest();
                handlers.get(i).sendAudioStream(request);
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("SpeechToTextService() failed");
        }
    }

}
