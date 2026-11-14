package com.dentalradar.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotNull(message = "Укажите оценку")
        @Min(value = 1, message = "Оценка должна быть от 1 до 5")
        @Max(value = 5, message = "Оценка должна быть от 1 до 5")
        Integer rating,
        @NotBlank(message = "Напишите текст отзыва")
        @Size(max = 2000, message = "Текст отзыва должен быть не длиннее 2000 символов")
        String text
) {
}
