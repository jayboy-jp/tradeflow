package com.tradeflow.security;

import com.tradeflow.exception.RateLimitException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final RedisRateLimiterService redisRateLimiterService;

    public AuthRateLimitFilter(RedisRateLimiterService redisRateLimiterService) {
        this.redisRateLimiterService = redisRateLimiterService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isAuthEndpoint(request.getRequestURI())) {
            String clientIp = request.getRemoteAddr();
            boolean allowed = redisRateLimiterService.allowAuthRequest(clientIp);
            if (!allowed) {
                throw new RateLimitException("Too many auth requests. Try again in a minute.");
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAuthEndpoint(String uri) {
        return uri != null && uri.startsWith("/api/v1/auth/");
    }
}
