package org.sujine.reacttosoundapi.qna.service;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
public class OpenAIService {
    @Value("${openai.api.key}")
    private String apiKey;
    @Value("${persona.info}")
    public String personaInfo;
    private final HttpClient client;
    private final JsonObjectBuilder jsonBuilder;

    public OpenAIService() {
        this.client = HttpClient.newHttpClient();
        this.jsonBuilder = Json.createObjectBuilder().add("model", "gpt-3.5-turbo");
        this.jsonBuilder.add("temperature", 0.7);
        this.jsonBuilder.add("top_p", 1);
        this.jsonBuilder.add("frequency_penalty", 0);
        this.jsonBuilder.add("presence_penalty", 0);
    }

    public String askGpt(String question) throws Exception {
        JsonArrayBuilder messagesBuilder = Json.createArrayBuilder();
        messagesBuilder.add(Json.createObjectBuilder().add("role", "system").add("content", "너는 또 다른 나야"));
        messagesBuilder.add(Json.createObjectBuilder().add("role", "system").add("content", this.personaInfo));
        messagesBuilder.add(Json.createObjectBuilder().add("role", "user").add("content", question));
        this.jsonBuilder.add("messages", messagesBuilder.build());

        JsonObject jsonRequest = jsonBuilder.build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        try (JsonReader jsonReader = Json.createReader(new StringReader(response.body()))) {
            JsonObject responseBody = jsonReader.readObject();
            return responseBody.getJsonArray("choices")
                    .getJsonObject(0)
                    .getJsonObject("message")
                    .getString("content");
        }
    }


}
