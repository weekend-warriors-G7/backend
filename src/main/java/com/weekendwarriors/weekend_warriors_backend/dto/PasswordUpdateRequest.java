package com.weekendwarriors.weekend_warriors_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordUpdateRequest {
    @NotBlank(message = "Password required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,64}$",
            message = "Password must contain at least 8 characters and at most 64 characters, including one uppercase letter, one lowercase letter, one digit, and one special character."
    )
    String oldPassword;
    @NotBlank(message = "Password required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,64}$",
            message = "Password must contain at least 8 characters and at most 64 characters, including one uppercase letter, one lowercase letter, one digit, and one special character."
    )
    String newPassword;
}
