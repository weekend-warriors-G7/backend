package com.weekendwarriors.weekend_warriors_backend.controller;

import com.weekendwarriors.weekend_warriors_backend.dto.AuthenticationWithCredentialsRequest;
import com.weekendwarriors.weekend_warriors_backend.dto.RefreshTokenRequest;
import com.weekendwarriors.weekend_warriors_backend.dto.RegisterWithCredentialsRequest;
import com.weekendwarriors.weekend_warriors_backend.dto.TokenResponse;
import com.weekendwarriors.weekend_warriors_backend.exception.ExistingUser;
import com.weekendwarriors.weekend_warriors_backend.exception.InvalidToken;
import com.weekendwarriors.weekend_warriors_backend.exception.UserNotFound;
import com.weekendwarriors.weekend_warriors_backend.service.AuthenticationWithCredentialsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationWithCredentialsService authenticationService;

    @Operation(summary = "Register a new user",
            description = "Creates a new user with the provided credentials: email, password, firstName and lastName")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400",
                    description = "Incorrect input",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "409",
                    description = "User already exists",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterWithCredentialsRequest registerData) throws ExistingUser {
        authenticationService.register(registerData);
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Successful register");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @Operation(summary = "Log in an existing user",
            description = "Authenticates a user with the provided credentials: email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User logged in successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthenticationWithCredentialsRequest loginData) throws UserNotFound {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Successful login");
        responseBody.put("token", authenticationService.login(loginData));
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshToken) throws InvalidToken {
        Map<String, TokenResponse> responseBody = new HashMap<>();
        responseBody.put("token", authenticationService.refreshToken(refreshToken));
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }
}
