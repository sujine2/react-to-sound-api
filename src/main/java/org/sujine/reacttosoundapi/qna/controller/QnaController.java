package org.sujine.reacttosoundapi.qna.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sujine.reacttosoundapi.qna.domain.ExampleQuestion;
import org.sujine.reacttosoundapi.qna.domain.Qna;
import org.sujine.reacttosoundapi.qna.dto.QuestionRequest;
import org.sujine.reacttosoundapi.qna.dto.Response;
import org.sujine.reacttosoundapi.qna.service.QnaService;
import org.sujine.reacttosoundapi.qna.jwt.JwtUtil;

import java.util.List;

@RestController
public class QnaController {
    private final QnaService qnaService;

    public QnaController(QnaService qnaService) {
        this.qnaService = qnaService;
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
        Response response = qnaService.getAnswer(jwt, request.getQuestion());
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
