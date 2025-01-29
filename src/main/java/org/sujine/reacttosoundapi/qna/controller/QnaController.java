package org.sujine.reacttosoundapi.question.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sujine.reacttosoundapi.question.domain.ExampleQuestion;
import org.sujine.reacttosoundapi.question.domain.Qna;
import org.sujine.reacttosoundapi.question.dto.QuestionRequest;
import org.sujine.reacttosoundapi.question.dto.Response;
import org.sujine.reacttosoundapi.question.event.QuestionSubmittedEvent;
import org.sujine.reacttosoundapi.question.jwt.JwtUtil;

import java.util.List;

@RestController
public class QnaController {
    private final ApplicationEventPublisher eventPublisher;

    QnaController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @GetMapping("/token/initialize")
    public ResponseEntity<String> tokenInitialize(HttpServletResponse response, @CookieValue(value = "jwt", required = false) String jwt) {
        if (jwt == null | !JwtUtil.isValidToken(jwt)) {
            String token = JwtUtil.generateToken();

            ResponseCookie cookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    //.secure(true)
                    .sameSite("Strict")
                    .path("/")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());
            return ResponseEntity.ok("Initialize JWT");
        } else return new ResponseEntity<>("Already exist JWT", new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE);
    }

    @PostMapping("/ask")
    public ResponseEntity<Response> ask(@CookieValue(value = "jwt", required = false) String jwt, @RequestBody QuestionRequest request)  throws Exception {
        this.eventPublisher.publishEvent(new QuestionSubmittedEvent(request.getQuestion(), jwt));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public List<Qna> history(@CookieValue(value = "jwt", required = false) String jwt) {
        return qnaService.getHistories(jwt);
    }

    @GetMapping("/ex/questions")
    public List<ExampleQuestion> exampleQuestions() {
        return qnaService.getExampleQuestions();
    }
}
