package com.stroll.www.filter;

import com.stroll.www.controller.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
//포워딩 시 재필터링 X를 위함

@Component
public class AuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    public AuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    //필터링을 스킵할 api
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 완전히 제외할 경로 있으면 추가(정적 리소스, 헬스체크 등)
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) { //jwt 표준
            String token = auth.substring(7);
            try {
                var claims = jwtService.parseClaims(token); // 서명/만료 검증 포함
                // 성공하면 요청 속성에 심어둠
                req.setAttribute("auth.userId", claims.getSubject());
                req.setAttribute("petType", claims.get("petType"));
            } catch (Exception ignore) {
                // 실패해도 막지 않고 '게스트'로 계속 진행
            }
        }
        chain.doFilter(req, res);
    }
}
