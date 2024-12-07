package com.weekendwarriors.weekend_warriors_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LastNameUpdateRequest {
    @NotBlank(message = "Last name required")
    String lastName;
}

