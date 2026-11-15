package com.dentalradar.dto;

import java.time.Instant;

public record UserReviewResponse(
        String clinicId,
        String clinicName,
        int rating,
        String text,
        Instant createdAt
) {
}
