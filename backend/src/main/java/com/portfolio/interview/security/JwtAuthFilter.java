package com.portfolio.interview.security;

import com.portfolio.interview.domain.user.UserEntity;
import com.portfolio.interview.repo.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtTokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // ✅ CORS preflight는 항상 통과
        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        // ✅ 공개 엔드포인트는 JWT 필터 자체를 스킵
        if ("/health".equals(path)) return true;

        // ⚠️ 너 SecurityConfig는 /api/auth/** 를 열고 있음
        // 여기 필터는 /auth/** 를 스킵하고 있으니 /api/auth/** 로 맞춰주는게 안전
        if (path.startsWith("/api/auth/")) return true;

        // ✅ actuator 전체 스킵 (health/readiness/liveness 포함)
        if (path.startsWith("/actuator/")) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // ✅ 토큰이 없으면 그냥 다음으로(인증 없이)
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            Jws<Claims> jws = tokenProvider.parse(token);
            Long userId = Long.valueOf(jws.getBody().getSubject());

            UserEntity user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                var auth = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        Collections.emptyList()
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            // ✅ 토큰이 잘못됐으면 인증 세팅 안 하고 그냥 진행
        }

        filterChain.doFilter(request, response);
    }
}