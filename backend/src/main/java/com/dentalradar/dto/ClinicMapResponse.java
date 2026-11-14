package com.dentalradar.dto;

import java.util.List;

public record ClinicMapResponse(
        String id,
        String name,
        String address,
        double latitude,
        double longitude,
        Double averageRating,
        int reviewCount,
        List<ServiceResponse> services
) {
}
