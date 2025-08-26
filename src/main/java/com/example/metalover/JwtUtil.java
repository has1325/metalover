package com.example.metalover;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;
    private final long validityMs = 1000L * 60 * 60 * 24; // 24h

    public JwtUtil() {
        String secret = System.getenv().getOrDefault("JWT_SECRET", "change_this_to_a_very_long_secret_key_!@#1234567890");
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();
    }

    public String getSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token, String username) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return claims.getExpiration().after(new Date()) && username.equals(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
