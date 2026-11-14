package com.dentalradar.dto;

public record AuthResponse(
        String token,
        String role,
        String displayName,
        String userId
) {
}
