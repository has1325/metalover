package com.example.metalover; // 본인 패키지명 사용

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // application 환경변수 JWT_SECRET 에 값이 없으면 빈 문자열로 기본화 (필요시 예외 처리)
    @Value("${JWT_SECRET:}")
    private String secret;

    private final long EXPIRATION = 1000L * 60 * 60; // 1시간

    private byte[] getSecretBytes() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    public String generateToken(String email) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, getSecretBytes())
                .compact();
    }

    public String extractEmail(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSecretBytes())
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(getSecretBytes()).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
