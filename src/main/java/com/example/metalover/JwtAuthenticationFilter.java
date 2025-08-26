package com.example.metalover;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String email = jwtUtil.getSubject(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = userDetailsService.loadUserByUsername(email);
                    if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                        var authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                // 토큰이 유효하지 않거나 parsing 실패하면 무시하고 진행 (필요시 로그 남기기)
            }
        }
        chain.doFilter(req, res);
    }
}
