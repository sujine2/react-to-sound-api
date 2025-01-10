package org.sujine.reacttosoundapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.sujine.reacttosoundapi.qna.controller.STTWebSocketHandler;
import org.sujine.reacttosoundapi.voiceColor.controller.VoiceColorWebSocketHandler;

@Configuration
@RequiredArgsConstructor
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final STTWebSocketHandler sttWebSocketHandler;
    private final VoiceColorWebSocketHandler voiceColorWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sttWebSocketHandler, "/speechToText")
                .setAllowedOrigins("*");

        registry.addHandler(voiceColorWebSocketHandler, "/voiceColor")
                .setAllowedOrigins("*");

    }
}
