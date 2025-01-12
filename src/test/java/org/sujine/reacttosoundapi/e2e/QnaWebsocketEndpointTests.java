package org.sujine.reacttosoundapi.e2e;

import jakarta.websocket.DeploymentException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketSession;
import org.sujine.reacttosoundapi.qna.dto.QuestionAudioStream;
import org.sujine.reacttosoundapi.qna.repository.QnaRepository;
import org.sujine.reacttosoundapi.qna.service.QnaService;
import org.sujine.reacttosoundapi.qna.service.utils.JwtUtil;
import org.sujine.reacttosoundapi.utils.StreamDataFactory;

import java.net.URI;
import java.net.http.WebSocketHandshakeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

// get jwt -> websocket connection -> Q&A (call openAI call) -> connection closed
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true",
        "jwt.secret.key=9OE/t4vS+y443u+e7yZ0yuS6rxSjhbFWutzrrylgOVM="
})
public class QnaWebsocketEndpointTests {
    @Autowired
    private QnaRepository qnaRepository;

    @Autowired
    private QnaService qnaService;

    private static final List<WebSocketSession> webSocketSessions = new ArrayList<>();
    private static final List<TestWebSocketClientHandler> handlers = new ArrayList<>();
    private static final List<String> JWTs = new ArrayList<>();
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final String WS_URI = "ws://localhost:%d/speechToText";
    private StreamDataFactory streamDataFactory;

    @BeforeEach
    void setUp() {
        streamDataFactory = new StreamDataFactory();

    }

    @BeforeAll
    void setUp(@LocalServerPort int port) throws ExecutionException, InterruptedException {
        createClientJWTs(port);
        createClientWebSocketConnections(port);
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

//    @Test
//    public void checkPersonaInfo() {
//        System.out.println(QnaService.personaInfo);
//    }

    @Test
    public void webSocketConnectionFailsWithInvalidJwtTest(@LocalServerPort int port) throws Exception {
        // Prepare URI and headers with an invalid JWT
        String uri = String.format("ws://localhost:%d/speechToText", port);

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add(WebSocketHttpHeaders.COOKIE, "jwt=invalid-jwt-token");

        StandardWebSocketClient client = new StandardWebSocketClient();
        TestWebSocketClientHandler handler = new TestWebSocketClientHandler();

        CompletableFuture<WebSocketSession> sessionFuture = client.execute(handler, headers, URI.create(uri));
        try {
            sessionFuture.get(5, TimeUnit.SECONDS);
            fail("Expected connection failure but succeeded.");
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof WebSocketHandshakeException) {
                WebSocketHandshakeException handshakeException = (WebSocketHandshakeException) cause;

                Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), handshakeException.getResponse().statusCode());
                Assertions.assertTrue(handshakeException.getMessage().contains("invalid JWT"));
                System.out.println("Connection failed as expected: " + handshakeException.getMessage());
            } else if (cause instanceof DeploymentException) {
                int statusCode = Integer.parseInt(cause.getMessage().replaceAll("[^0-9]", ""));
                Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), statusCode);
                Assertions.assertTrue(cause.getMessage().contains("HTTP response code"), "Unexpected server response");
                System.out.println("Connection failed due to DeploymentException: " + cause.getMessage());
            } else {
                fail("Unexpected exception type: " + cause.getClass());
            }
        }
    }


    @DisplayName("A client sends a request to the STT endpoint.")
    @Test
    void STTWebsocketEndpointTest(@LocalServerPort int port) {
        streamDataFactory.setAudioFormat((float) 16000, 16, false);
        QuestionAudioStream request = streamDataFactory.createSTTRequest();
        try {
            handlers.get(1).sendAudioStream(request);
            // String receivedMessage = TestWebSocketClientHandler.messageFuture.get();
            // System.out.println("Received message: " + receivedMessage);
            Thread.sleep(10000);

            // get history
            String url = String.format("http://localhost:%d/history", port);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.COOKIE, JWTs.get(1));
            HttpEntity<List> entity = new HttpEntity<>(headers);
            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class
            );
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());

            // different jwt
            headers = new HttpHeaders();
            headers.add(HttpHeaders.COOKIE, "jwt=aksjflasjfoifjo");
            entity = new HttpEntity<>(headers);
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class
            );
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            List<Map<String, Object>> body = response.getBody();
            Assertions.assertNotNull(body);
            Assertions.assertTrue(body.isEmpty(), "The response body is not an empty array.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("SpeechToTextService() failed");
        }
    }

    @DisplayName("Clients send requests to the STT endpoint.")
    @Test
    void STTWebsocketEndpointWithClientsTest(@LocalServerPort int port) {
        try {
            for (int i = 0; i < handlers.size(); i++) {
                streamDataFactory.setAudioFormat((float) 16000, 16, false);
                QuestionAudioStream request = streamDataFactory.createSTTRequest();
                handlers.get(i).sendAudioStream(request);
                Thread.sleep(10000);

                String url = String.format("http://localhost:%d/history", port);
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.COOKIE, JWTs.get(i));
                HttpEntity<List> entity = new HttpEntity<>(headers);
                ResponseEntity<List> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        List.class
                );
                Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
                System.out.println("===== user" +  (i + 1)  +" =====");
                System.out.println("Response Body: " + response.getBody());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("SpeechToTextService() failed");
        }
    }


    private static void createClientJWTs(@LocalServerPort int port){
        String url = String.format("http://localhost:%d/token/initialize", port);

        for (int i = 0; i < 3; i++) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cookie", "");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
            Assertions.assertEquals("Initialize JWT", response.getBody());
            List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            assertNotNull(cookies);

            String jwt = cookies.stream()
                    .filter(cookie -> cookie.startsWith("jwt="))
                    .findFirst()
                    .map(cookie -> cookie.substring(0, cookie.indexOf(";"))) // jwt= 이후 값 추출
                    .orElse(null);
            assertNotNull(jwt);
            System.out.println("Extracted JWT: " + jwt);
            JWTs.add(jwt);
        }

    }

    private static void createClientWebSocketConnections(@LocalServerPort int port)
            throws ExecutionException, InterruptedException {
        URI uri = URI.create(String.format(WS_URI, port));
        for (int i = 0; i < 3; i++) {
            StandardWebSocketClient client = new StandardWebSocketClient();
            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
            headers.add(WebSocketHttpHeaders.COOKIE, JWTs.get(i));
            handlers.add(new TestWebSocketClientHandler());

            webSocketSessions.add(client.execute(handlers.get(i), headers, uri).get());
        }
    }

}
