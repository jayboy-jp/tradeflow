package com.tradeflow.dto;

public record AuthTokensResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresInSeconds
) {
    public static AuthTokensResponse of(String accessToken, String refreshToken, long accessTokenExpiresInSeconds) {
        return new AuthTokensResponse(accessToken, refreshToken, "Bearer", accessTokenExpiresInSeconds);
    }
}
