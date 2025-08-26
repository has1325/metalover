package com.example.metalover;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private final String secretKey = System.getenv("JWT_SECRET_KEY"); // Render 환경변수 읽기
    private final long validityInMs = 1000 * 60 * 60; // 1시간

    // JWT 토큰 생성
    public String generateToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .setSubject(email) // 토큰에 email 저장
                .setIssuedAt(now)  // 발급 시간
                .setExpiration(validity) // 만료 시간
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // JWT 토큰에서 이메일 꺼내기
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
