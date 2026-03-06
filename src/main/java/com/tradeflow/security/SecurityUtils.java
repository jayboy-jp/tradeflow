package com.tradeflow.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the current authenticated user ID (set by JwtAuthFilter).
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof Long)) {
            return null;
        }
        return (Long) auth.getPrincipal();
    }

    public static Long requireCurrentUserId() {
        Long id = getCurrentUserId();
        if (id == null) {
            throw new com.tradeflow.exception.BusinessException("UNAUTHORIZED", "Authentication required");
        }
        return id;
    }
}
