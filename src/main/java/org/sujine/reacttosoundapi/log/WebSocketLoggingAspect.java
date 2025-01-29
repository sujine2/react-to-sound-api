package org.sujine.reacttosoundapi.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

@Aspect
@Component
public class WebSocketLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketLoggingAspect.class);

    @Around("execution(* *..*WebsocketHandler.afterConnectionEstablished(..))")
    public Object logAfterConnectionEstablished(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        WebSocketSession session = (WebSocketSession) args[0];

        logger.info("🔗 WebSocket connected - session ID: {}", session.getId());

        Object result = joinPoint.proceed();

        return result;
    }

    @Around("execution(* *..*WebsocketHandler.afterConnectionClosed(..))")
    public Object logAfterConnectionClosed(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        WebSocketSession session = (WebSocketSession) args[0];
        CloseStatus status = (CloseStatus) args[1];

        logger.info("🔌 WebSocket closed - session ID: {}, status: {}", session.getId(), status);

        return joinPoint.proceed();
    }

    @Around("execution(* *..*WebsocketHandler.handleTransportError(..))")
    public Object logHandleTransportError(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        WebSocketSession session = (WebSocketSession) args[0];
        Throwable exception = (Throwable) args[1];

        logger.error("🚨 WebSocket error - session ID: {}, message: {}", session.getId(), exception.getMessage());

        return joinPoint.proceed();
    }
}
