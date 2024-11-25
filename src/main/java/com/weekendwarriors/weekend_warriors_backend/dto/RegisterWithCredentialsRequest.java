package com.weekendwarriors.weekend_warriors_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class RegisterWithCredentialsRequest extends AuthenticationWithCredentialsRequest{
    @NotBlank(message = "First name required")
    @Pattern(
            regexp = "^[\\p{L} \\-]+$",
            message = "The first name must contain only alphabetic characters"
    )
    private String firstName;

    @NotBlank(message = "Last name required")
    @Pattern(
            regexp = "^[\\p{L} \\-]+$",
            message = "The last name must contain only alphabetic characters"
    )
    private String lastName;

    public RegisterWithCredentialsRequest(
            String email,
            String password,
            String firstName,
            String lastName
    ) {
        super(email, password);
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
