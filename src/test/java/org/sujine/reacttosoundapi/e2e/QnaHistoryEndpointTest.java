package org.sujine.reacttosoundapi.e2e;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.sujine.reacttosoundapi.qna.domain.Qna;
import org.sujine.reacttosoundapi.qna.service.QnaService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// http api -> request oracle cloud (mysql server)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QnaHistoryEndpointTest {
    @Autowired
    private QnaService qnaService;
    @Autowired
    private DataSource dataSource;

    private final static String userId = "testuser";
    private static final RestTemplate restTemplate = new RestTemplate();

    @BeforeAll
    void setup() {
        qnaService.saveQna("What is your favorite color?", "yellow.", userId);
        qnaService.saveQna("What is Spring?", "A Java framework.", userId);
        qnaService.saveQna("What is Java?", "A programming language.", userId);
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
        String url = String.format("http://localhost:%d/history", port);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "jwt=" + userId);
        HttpEntity<Qna[]> entity = new HttpEntity<>(headers);
        ResponseEntity<Qna[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Qna[].class
        );
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        Qna[] qnaList = response.getBody();
        Assertions.assertNotNull(qnaList);

        Assertions.assertEquals("What is your favorite color?", qnaList[0].getQuestion());
        Assertions.assertEquals("yellow.", qnaList[0].getAnswer());
        Assertions.assertEquals("testuser", qnaList[0].getUserId());

        Assertions.assertEquals("What is Spring?", qnaList[1].getQuestion());
        Assertions.assertEquals("A Java framework.", qnaList[1].getAnswer());
        Assertions.assertEquals("testuser", qnaList[1].getUserId());

        Assertions.assertEquals("What is Java?", qnaList[2].getQuestion());
        Assertions.assertEquals("A programming language.", qnaList[2].getAnswer());
        Assertions.assertEquals("testuser", qnaList[2].getUserId());
    }
}
