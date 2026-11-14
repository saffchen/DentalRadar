package com.dentalradar.dto;

import java.time.Instant;

public record ReviewResponse(
        String id,
        String authorName,
        int rating,
        String text,
        Instant createdAt,
        boolean mine
) {
}
