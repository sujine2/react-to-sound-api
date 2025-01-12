package org.sujine.reacttosoundapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.sujine.reacttosoundapi.qna.controller.QnaController;
import org.sujine.reacttosoundapi.qna.service.QnaService;
import org.sujine.reacttosoundapi.qna.service.utils.JwtUtil;

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

    @DisplayName("request JWT first")
    @Test
    void tokenInitializeWithNoJwtCookie() throws Exception {
        String mockToken = "mock.jwt.token";
        Mockito.when(JwtUtil.generateToken()).thenReturn(mockToken);
        Mockito.when(JwtUtil.isValidToken(Mockito.anyString())).thenReturn(false);

        MvcResult result = mockMvc.perform(get("/token/initialize"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, "jwt=" + mockToken + "; HttpOnly; Path=/"))
                .andExpect(content().string("Initialize JWT"))
                .andReturn();

        String setCookieHeader = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        System.out.println("Set-Cookie Header: " + setCookieHeader);
    }

    @DisplayName("request JWT with invalid cookie")
    @Test
    void testTokenInitializeWithInvalidJwtCookie() throws Exception {
        String mockToken = "mock.jwt.token";
        Mockito.when(JwtUtil.generateToken()).thenReturn(mockToken);
        Mockito.when(JwtUtil.isValidToken("invalid.jwt.token")).thenReturn(false);

        MvcResult result = mockMvc.perform(get("/token/initialize"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, "jwt=" + mockToken + "; HttpOnly; Path=/"))
                .andExpect(content().string("Initialize JWT"))
                .andReturn();

        String setCookieHeader = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        System.out.println("Set-Cookie Header: " + setCookieHeader);
    }

    @DisplayName("request JWT with valid cookie")
    @Test
    void testTokenInitializeWithValidJwtCookie() throws Exception {
        Mockito.when(JwtUtil.isValidToken("valid.jwt.token")).thenReturn(true);

        mockMvc.perform(get("/token/initialize")
                        .header("Cookie", "jwt=valid.jwt.token"))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().string("Already exist JWT"));
    }
}
