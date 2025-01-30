package org.sujine.reacttosoundapi.stt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@RequiredArgsConstructor
@EnableWebSocket
class STTWebSocketConfig implements WebSocketConfigurer {

    private final STTWebsocketHandler sttWebsocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sttWebsocketHandler, "/speechToText")
                .addInterceptors(new JwtHandshakeInterceptor())
                .setAllowedOrigins("*");
    }
}
