package org.sujine.reacttosoundapi.stt.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.sujine.reacttosoundapi.jwt.JwtUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        List<String> cookies = request.getHeaders().get(HttpHeaders.COOKIE);
        if (cookies == null || cookies.isEmpty()) {
            return false;
        }

        Optional<String> jwtToken = cookies.stream()
                .flatMap(cookieHeader -> List.of(cookieHeader.split(";")).stream())
                .map(String::trim)
                .filter(cookie -> cookie.startsWith("jwt="))
                .map(cookie -> cookie.substring(4))
                .findFirst();

        if (jwtToken.isPresent() && JwtUtil.isValidToken(jwtToken.get())) {
            attributes.put("userId", jwtToken.get());
            return true;
        }

        if (response instanceof ServletServerHttpResponse) {
            ((ServletServerHttpResponse) response).getServletResponse().sendError(
                    HttpStatus.UNAUTHORIZED.value(), "Invalid JWT"
            );
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

