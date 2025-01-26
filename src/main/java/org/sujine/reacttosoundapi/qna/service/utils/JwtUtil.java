package org.sujine.reacttosoundapi.qna.service.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    private static String secretKey;
    private static final long expirationTime = 1800000; // 30 minutes

    public JwtUtil(@Value("${jwt.secret.key}") String secretKey) {
        JwtUtil.secretKey = secretKey;
    }


    public static String generateToken() {
        String uniqueId = UUID.randomUUID().toString();
        return Jwts.builder()
                .setSubject(uniqueId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // expired time
                .signWith(getSigningKey())
                .compact();
    }

    public static boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }
}
