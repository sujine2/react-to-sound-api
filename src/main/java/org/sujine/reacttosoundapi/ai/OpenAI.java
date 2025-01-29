package org.sujine.reacttosoundapi.ai;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OpenAI implements AIClient {
    @Value("${persona.info}")
    private String personaInfo;
    private final OpenAiChatClient chatClient;

    public OpenAI(@Value("${spring.ai.openai.api-key}") String apiKey) {
        OpenAiApi openAiApi = new OpenAiApi(apiKey);
        this.chatClient = new OpenAiChatClient(openAiApi);
    }

    @Override
    public String generateResponse(String question) {
        List<Message> messages = List.of(
                new SystemMessage( "너는 또 다른 나야"),
                new SystemMessage(this.personaInfo),
                new UserMessage(question)
        );

        return chatClient
                .call(new Prompt(messages))
                .getResult().getOutput().getContent();
    }

}
