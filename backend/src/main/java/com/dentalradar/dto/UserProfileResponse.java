package com.dentalradar.dto;

import java.util.List;

public record UserProfileResponse(
        String id,
        String email,
        String displayName,
        String role,
        List<UserReviewResponse> reviews
) {
}
