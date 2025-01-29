package org.sujine.reacttosoundapi.voice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@RequiredArgsConstructor
@EnableWebSocket
class VoiceStreamWebSocketConfig implements WebSocketConfigurer {
    private final VoiceColorWebSocketHandler voiceColorWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(voiceColorWebSocketHandler, "/voiceColor")
                .setAllowedOrigins("*");

    }
}
