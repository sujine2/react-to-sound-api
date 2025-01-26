package org.sujine.reacttosoundapi.unit.controller;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.sujine.reacttosoundapi.qna.controller.QnaController;
import org.sujine.reacttosoundapi.qna.service.QnaService;
import org.sujine.reacttosoundapi.qna.jwt.JwtUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QnaController.class)
public class QnaHttpEndpointTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QnaService qnaService;
    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "staticSecretKey", "9OE/t4vS+y443u+e7yZ0yuS6rxSjhbFWutzrrylgOVM=");
    }

    @DisplayName("request JWT first")
    @Test
    void tokenInitializeWithNoJwtCookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/token/initialize"))
                .andExpect(status().isOk())
                .andExpect(content().string("Initialize JWT"))
                .andReturn();

        String setCookieHeader = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        System.out.println("Set-Cookie Header: " + setCookieHeader);
    }

    @DisplayName("request JWT with invalid cookie")
    @Test
    void tokenInitializeWithInvalidJwtCookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/token/initialize").cookie(new Cookie("jwt", "aaaa")))
                .andExpect(status().isOk())
                .andExpect(content().string("Initialize JWT"))
                .andReturn();

        String setCookieHeader = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        System.out.println("Set-Cookie Header: " + setCookieHeader);
    }

    @DisplayName("request JWT with valid cookie")
    @Test
    void tokenInitializeWithValidJwtCookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/token/initialize")).andReturn();
        String cookies = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        String jwt = cookies.split(";")[0].substring(4);

        mockMvc.perform(get("/token/initialize").cookie(new Cookie("jwt", jwt)))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().string("Already exist JWT"));
    }
}
