package com.dentalradar.dto;

import java.math.BigDecimal;

public record ServiceResponse(
        String id,
        String name,
        BigDecimal price
) {
}
