package com.dentalradar.dto;

import com.dentalradar.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Укажите email")
        @Email(message = "Некорректный email")
        String email,
        @NotBlank(message = "Укажите пароль")
        @Size(min = 8, message = "Пароль должен содержать не меньше 8 символов")
        String password,
        @NotBlank(message = "Укажите имя")
        @Size(max = 120, message = "Имя должно быть не длиннее 120 символов")
        String displayName,
        @NotNull(message = "Выберите роль")
        Role role
) {
}
