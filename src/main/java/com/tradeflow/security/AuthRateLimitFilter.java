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
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 25;
    private static final long WINDOW_SECONDS = 60;

    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isAuthEndpoint(request.getRequestURI())) {
            String key = request.getRemoteAddr();
            WindowCounter counter = counters.computeIfAbsent(key, k -> new WindowCounter());
            synchronized (counter) {
                long now = Instant.now().getEpochSecond();
                if (now - counter.windowStartEpochSeconds >= WINDOW_SECONDS) {
                    counter.windowStartEpochSeconds = now;
                    counter.count.set(0);
                }
                if (counter.count.incrementAndGet() > MAX_REQUESTS) {
                    throw new RateLimitException("Too many auth requests. Try again in a minute.");
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAuthEndpoint(String uri) {
        return uri != null && uri.startsWith("/api/v1/auth/");
    }

    private static class WindowCounter {
        private long windowStartEpochSeconds = Instant.now().getEpochSecond();
        private final AtomicInteger count = new AtomicInteger(0);
    }
}
