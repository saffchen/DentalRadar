package com.dentalradar.dto;

import java.util.List;

public record ClinicProfileResponse(
        String id,
        String name,
        String description,
        String address,
        Double latitude,
        Double longitude,
        Double averageRating,
        int reviewCount,
        List<ServiceResponse> services,
        List<ReviewResponse> reviews,
        String warning
) {
}
