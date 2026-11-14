package com.dentalradar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClinicProfileRequest(
        @NotBlank(message = "Укажите название клиники")
        @Size(max = 120, message = "Название должно быть не длиннее 120 символов")
        String name,
        @Size(max = 2000, message = "Описание должно быть не длиннее 2000 символов")
        String description,
        @Size(max = 300, message = "Адрес должен быть не длиннее 300 символов")
        String address
) {
}
