package org.sujine.reacttosoundapi.speechToText.service;

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

public class OpenAIService {
    //private static final String configPath = "src/main/java/org/sujine/reacttosoundapi/speechToText/config/apikey.properties";
    private static final String configPath = "../../../react-to-sound-api/src/main/java/org/sujine/reacttosoundapi/speechToText/config/apikey.properties";
    private static String apiKey;
    private static final String personaInfo = """
        저는 지니입니다.
        저는 블록체인 컨트랙트 개발자로 2년간 일했습니다.
        현재는 소프트웨어 엔지니어를 꿈꾸고 있습니다.
        상상을 현실로 만들어내고 싶어요.
        저는 컴퓨터 언어 Java, Go, Solidity, Python을 사용할 줄 압니다.
        저는 WEMADE 블록체인 콘텐츠 개발팀에서 8개월, Bifrost Chain 네트워크 팀에서 1년, Elysia에서 인턴으로 6개월 일했습니다.
        저는 요리하는 것을 좋아합니다.
        좋아하는 색은 하늘색과 분홍색입니다.
        저는 고양이를 좋아합니다.
        고양이를 키우고 있고, 제가 키우는 고양이 이름은 코코입니다.
        저는 2001년 12월 19일에 태어났습니다.
    """;

    public OpenAIService() {
        System.out.println("현재 작업 디렉토리: " + System.getProperty("user.dir"));

        try (FileInputStream input = new FileInputStream(configPath)) {
            Properties prop = new Properties();
            prop.load(input);

            // API 키 가져오기
            apiKey = prop.getProperty("OPEN_AI");
            System.out.println("API Key: " + apiKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String askGpt(String question) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // JSON 객체 생성 (페르소나 정보 및 질문 추가)
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
                .add("model", "gpt-3.5-turbo");

        // Messages 배열 생성 (system, user 메시지)
        JsonArrayBuilder messagesBuilder = Json.createArrayBuilder();
        messagesBuilder.add(Json.createObjectBuilder().add("role", "system").add("content", "너는 또 다른 나야"));
        messagesBuilder.add(Json.createObjectBuilder().add("role", "system").add("content", personaInfo));
        messagesBuilder.add(Json.createObjectBuilder().add("role", "user").add("content", question));

        jsonBuilder.add("messages", messagesBuilder.build());
        jsonBuilder.add("temperature", 0.7);
        jsonBuilder.add("top_p", 1);
        jsonBuilder.add("frequency_penalty", 0);
        jsonBuilder.add("presence_penalty", 0);

        JsonObject jsonRequest = jsonBuilder.build();

        // HTTP 요청 생성
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .build();

        // 요청 보내기 및 응답 받기
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
