package com.dentalradar.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServiceRequest(
        @NotBlank(message = "Укажите название услуги")
        @Size(max = 120, message = "Название услуги должно быть не длиннее 120 символов")
        String name,
        @NotNull(message = "Укажите цену")
        @DecimalMin(value = "0.01", message = "Цена должна быть больше нуля")
        @Digits(integer = 8, fraction = 2, message = "Цена должна содержать не больше двух знаков после запятой")
        BigDecimal price
) {
}
