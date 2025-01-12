package org.sujine.reacttosoundapi.qna.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.sujine.reacttosoundapi.qna.service.utils.JwtUtil;

import java.util.List;
import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        List<String> cookies = request.getHeaders().get("cookie");
        if (cookies != null || !cookies.isEmpty()) {
            for (String cookie : cookies) {
                if (cookie.startsWith("jwt=")) {
                    String jwt = cookie.substring(4); // "jwt="
                    if (JwtUtil.isValidToken(jwt)) {
                        attributes.put("userId", jwt);
                        return true;
                    }  else {
                        if (response instanceof ServletServerHttpResponse) {
                            ((ServletServerHttpResponse) response).getServletResponse().sendError(
                                    HttpStatus.UNAUTHORIZED.value(), "invalid JWT"
                            );
                        }
                    }

                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(
            org.springframework.http.server.ServerHttpRequest request,
            org.springframework.http.server.ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception ex) {

    }
}

