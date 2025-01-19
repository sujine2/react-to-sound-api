package org.sujine.reacttosoundapi.qna.service;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sujine.reacttosoundapi.qna.domain.ExampleQuestion;
import org.sujine.reacttosoundapi.qna.domain.Qna;
import org.sujine.reacttosoundapi.qna.dto.Response;
import org.sujine.reacttosoundapi.qna.repository.QnaRepository;

@Service
public class QnaService {
    @Value("${openai.api.key}")
    private String apiKey;
    @Value("${persona.info}")
    private String personaInfo;
    private final QnaRepository qnaRepository;
    private final HttpClient client;

    @Autowired
    public QnaService(QnaRepository qnaRepository) {
        this.client = HttpClient.newHttpClient();
        this.qnaRepository = qnaRepository;
    }

    @Transactional
    public Response getAnswer(String jwt, String question) throws Exception {
        // call OpenAI API
        String answer = this.requestOpenAI(question);
        // save answer
        this.saveQna(question, answer, jwt);
        // send answer
        return new Response(answer, true, false);
    }

    @Transactional
    public Qna saveQna(String question, String answer, String userId) {
        return this.qnaRepository.save(new Qna(question, answer, userId));
    }

    @Transactional
    public List<Qna> getHistories(String userId) {
        return this.qnaRepository.findByUserId(userId);
    }

    @Transactional
    public List<ExampleQuestion> getExampleQuestions() {
        List<Object[]> results = this.qnaRepository.findRandomQuestions(2);
        return results.stream()
                .map(result -> new ExampleQuestion(
                        ((Number) result[0]).longValue(),
                        (String) result[1]
                )) // Object[] → DTO
                .collect(Collectors.toList());
    }

    public String requestOpenAI(String question) throws Exception {
        JsonArrayBuilder messagesBuilder = Json.createArrayBuilder();
        messagesBuilder.add(Json.createObjectBuilder().add("role", "system").add("content", "너는 또 다른 나야"));
        messagesBuilder.add(Json.createObjectBuilder().add("role", "system").add("content", personaInfo));
        messagesBuilder.add(Json.createObjectBuilder().add("role", "user").add("content", question));

        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
                .add("model", "gpt-3.5-turbo") // 모델 추가
                .add("temperature", 0.7)
                .add("top_p", 1)
                .add("frequency_penalty", 0)
                .add("presence_penalty", 0)
                .add("messages", messagesBuilder.build());
        JsonObject jsonRequest = jsonBuilder.build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            try (JsonReader jsonReader = Json.createReader(new StringReader(response.body()))) {
                JsonObject responseBody = jsonReader.readObject();
                return responseBody.getJsonArray("choices")
                        .getJsonObject(0)
                        .getJsonObject("message")
                        .getString("content");
            }
        } else {
            throw new RuntimeException("Unexpected response status: " + response.statusCode()
                    + " with body: " + response.body());
        }
    }

}
