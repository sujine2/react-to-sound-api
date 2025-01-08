package org.sujine.reacttosoundapi.qna.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenAIService {
    @Value("${openai.api.key}")
    private String apiKey;
    @Value("${persona.info}")
    private String personaInfo;

    public String askGpt(String question) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
                .add("model", "gpt-3.5-turbo");

        System.out.println(this.personaInfo);
        JsonArrayBuilder messagesBuilder = Json.createArrayBuilder();
        messagesBuilder.add(Json.createObjectBuilder().add("role", "system").add("content", "너는 또 다른 나야"));
        messagesBuilder.add(Json.createObjectBuilder().add("role", "system").add("content", this.personaInfo));
        messagesBuilder.add(Json.createObjectBuilder().add("role", "user").add("content", question));

        jsonBuilder.add("messages", messagesBuilder.build());
        jsonBuilder.add("temperature", 0.7);
        jsonBuilder.add("top_p", 1);
        jsonBuilder.add("frequency_penalty", 0);
        jsonBuilder.add("presence_penalty", 0);

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
