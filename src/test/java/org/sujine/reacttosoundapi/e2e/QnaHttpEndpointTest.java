package org.sujine.reacttosoundapi.e2e;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import org.sujine.reacttosoundapi.ai.AIClient;
import org.sujine.reacttosoundapi.ai.OpenAI;
import org.sujine.reacttosoundapi.qna.domain.Qna;
import org.sujine.reacttosoundapi.qna.dto.Question;
import org.sujine.reacttosoundapi.qna.repository.QnaRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.Assert.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
public class QnaHttpEndpointTest {
    @Autowired
    private OpenAI openAI;
    @Autowired
    private QnaRepository qnaRepository;
    @Autowired
    private DataSource dataSource;

    private static String userId;
    private static String baseUrl;
    private static final RestTemplate restTemplate = new RestTemplate();

    @BeforeAll
    void setup(@LocalServerPort int port) {
        baseUrl =  String.format("http://localhost:%d", port);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/token/initialize",
                HttpMethod.GET,
                null,
                String.class
        );
        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        userId = setCookie.split(";")[0].split("=")[1];;
    }


    @DisplayName("Try external oracle server")
    @Test
    public void externalDatabaseConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Assertions.assertNotNull(connection, "Database connection should not be null");

            System.out.println("Connected to: " + connection.getMetaData().getURL());
        } catch (SQLException e) {
            Assertions.fail("Failed to connect to the database: " + e.getMessage());
        }
    }

    @DisplayName("Return Q&A history from external oracle server")
    @Test
    void getHistoryTest(@LocalServerPort int port) {
        qnaRepository.save(new Qna("What is your favorite color?", "yellow.", userId));
        qnaRepository.save(new Qna("What is Spring?", "A Java framework.", userId));
        qnaRepository.save(new Qna("What is Java?", "A programming language.", userId));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "jwt=" + userId);
        HttpEntity<Qna[]> entity = new HttpEntity<>(headers);
        ResponseEntity<Qna[]> response = restTemplate.exchange(
                baseUrl + "/history",
                HttpMethod.GET,
                entity,
                Qna[].class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        Qna[] qnaList = response.getBody();
        Assertions.assertNotNull(qnaList);

        Assertions.assertEquals("What is your favorite color?", qnaList[0].getQuestion());
        Assertions.assertEquals("yellow.", qnaList[0].getAnswer());
        Assertions.assertEquals(userId, qnaList[0].getUserId());

        Assertions.assertEquals("What is Spring?", qnaList[1].getQuestion());
        Assertions.assertEquals("A Java framework.", qnaList[1].getAnswer());
        Assertions.assertEquals(userId, qnaList[1].getUserId());

        Assertions.assertEquals("What is Java?", qnaList[2].getQuestion());
        Assertions.assertEquals("A programming language.", qnaList[2].getAnswer());
        Assertions.assertEquals(userId, qnaList[2].getUserId());
    }


    @DisplayName("verify asynchronous request")
    @Test
    void verifyAsyncRequest(@LocalServerPort int port) throws Exception {
        int numberOfRequests = 5;
        String url = baseUrl + "/ask";
        ExecutorService executor = Executors.newFixedThreadPool(numberOfRequests);
        CompletionService<ResponseEntity<String>> completionService = new ExecutorCompletionService<>(executor);
        Map<Future<ResponseEntity<String>>, String> requestMap = new ConcurrentHashMap<>();

        List<String> questions = List.of(
                "How does deep learning work?",
                "What is AI?",
                "Tell me a joke.",
                "Translate 'hello' to French.",
                "What is the capital of Japan?"
        );

        for (String question : questions) {
            Future<ResponseEntity<String>> future = completionService.submit(() -> sendRequest(url, question));
            requestMap.put(future, question);
        }

        for (int i = 0; i < questions.size(); i++) {
            Future<ResponseEntity<String>> future = completionService.take();
            ResponseEntity<String> response = future.get();
            String originalQuestion = requestMap.get(future);

            assertNotNull(response.getBody());
            System.out.println("Request: " + originalQuestion + " → Response: " + response.getBody());
        }

        executor.shutdown();
    }

    private ResponseEntity<String> sendRequest(String url, String question) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.COOKIE, "jwt=" + userId);

        String jsonBody = "{\"question\": \"" + question + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );
    }
}
