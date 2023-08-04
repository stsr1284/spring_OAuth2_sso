package com.cos.security.model;

import lombok.Data;

@Data
public class KakaoLoginRequest {
    private String accessToken;
    private String accessTokenExpiresAt;
    private String refreshToken;
    private String refreshTokenExpiresAt;
    private String id;
    private String provider;

    // Add getters and setters
}
