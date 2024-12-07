package com.weekendwarriors.weekend_warriors_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FirstNameUpdateRequest {
    @NotBlank(message = "First name required")
    String firstName;
}
