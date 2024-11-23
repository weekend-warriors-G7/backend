package com.weekendwarriors.weekend_warriors_backend;

import com.weekendwarriors.weekend_warriors_backend.dto.AuthenticationWithCredentialsRequest;
import com.weekendwarriors.weekend_warriors_backend.dto.RegisterWithCredentialsRequest;
import com.weekendwarriors.weekend_warriors_backend.dto.TokenResponse;
import com.weekendwarriors.weekend_warriors_backend.exception.ExistingUser;
import com.weekendwarriors.weekend_warriors_backend.exception.UserNotFound;
import com.weekendwarriors.weekend_warriors_backend.service.AuthenticationWithCredentialsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private AutoCloseable mocks;

    @MockBean
    private AuthenticationWithCredentialsService authService;

    @BeforeEach
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void register_correctInputAndNonExistingEmail_returnsUserRegisteredSuccessfullyMessage() throws Exception {
        //ARRANGE
        String userEmail = "test@test.com";
        String userPassword = "TESTtest1234!";
        String firstName = "user";
        String lastName = "test";
        String registerJsonData = "{\"email\":\"%s\",\"password\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"}".formatted(userEmail, userPassword, firstName, lastName);

        //ACT
        ResultActions result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJsonData));

        //ASSERT
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Successful register"));

        verify(authService).register(any(RegisterWithCredentialsRequest.class));
    }

    @Test
    public void register_correctInputAndExistingEmail_returnsErrorMessage() throws Exception {
        // ARRANGE
        String userEmail = "existing@test.com";
        String userPassword = "TESTtest1234!";
        String firstName = "user";
        String lastName = "test";
        String registerJsonData = "{\"email\":\"%s\",\"password\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"}".formatted(userEmail, userPassword, firstName, lastName);

        Mockito.doThrow(new ExistingUser("Email already in use")).when(authService).register(any(RegisterWithCredentialsRequest.class));

        // ACT
        ResultActions result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJsonData));

        // ASSERT
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already in use"));

        verify(authService).register(any(RegisterWithCredentialsRequest.class));
    }

    @Test
    public void register_incorrectInputMissingField_returnsErrorMessage() throws Exception {
        // ARRANGE
        String registerJsonData = "{\"email\":\"\",\"password\":\"TESTtest1234!\",\"firstName\":\"firstname\",\"lastName\":\"lastname\"}";

        // ACT
        ResultActions result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJsonData));

        // ASSERT
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect input"));

        verify(authService, Mockito.never()).register(any(RegisterWithCredentialsRequest.class));
    }

    @Test
    public void register_incorrectInputForEmail_returnsErrorMessage() throws Exception {
        // ARRANGE
        String registerJsonData = "{\"email\":\"invalid-email\",\"password\":\"TESTtest1234!\",\"firstName\":\"firstname\",\"lastName\":\"lastname\"}";

        // ACT
        ResultActions result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJsonData));

        // ASSERT
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect input"));

        verify(authService, Mockito.never()).register(any(RegisterWithCredentialsRequest.class));
    }

    @Test
    public void register_incorrectInputWeakPassword_returnsErrorMessage() throws Exception {
        // ARRANGE
        String registerJsonData = "{\"email\":\"email@test.com\",\"password\":\"test\",\"firstName\":\"firstname\",\"lastName\":\"lastname\"}";

        // ACT
        ResultActions result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJsonData));

        // ASSERT
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Incorrect input"));

        verify(authService, Mockito.never()).register(any(RegisterWithCredentialsRequest.class));
    }

    @Test
    public void login_correctCredentials_returnsSuccessMessage() throws Exception {
        // ARRANGE
        String userEmail = "test@test.com";
        String userPassword = "TESTtest1234!";
        String loginJsonData = "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(userEmail, userPassword);

        Mockito.when(authService.login(any(AuthenticationWithCredentialsRequest.class)))
                .thenReturn(new TokenResponse("access-token", "refresh-token"));

        // ACT
        ResultActions result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJsonData));

        // ASSERT
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successful login"));

        verify(authService).login(any(AuthenticationWithCredentialsRequest.class));
    }

    @Test
    public void login_unregisteredEmail_returnsErrorMessage() throws Exception {
        // ARRANGE
        String userEmail = "notfound@test.com";
        String userPassword = "TESTtest1234!";
        String loginJsonData = "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(userEmail, userPassword);

        Mockito.doThrow(new UserNotFound("Invalid credentials provided")).when(authService).login(any(AuthenticationWithCredentialsRequest.class));

        // ACT
        ResultActions result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJsonData));

        // ASSERT
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Invalid credentials provided"));

        verify(authService).login(any(AuthenticationWithCredentialsRequest.class));
    }

    @Test
    public void login_incorrectPassword_returnsErrorMessage() throws Exception {
        // ARRANGE
        String userEmail = "test@test.com";
        String userPassword = "wrongPassword1234!";
        String loginJsonData = "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(userEmail, userPassword);

        Mockito.doThrow(new UserNotFound("Invalid credentials provided")).when(authService).login(any(AuthenticationWithCredentialsRequest.class));

        // ACT
        ResultActions result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJsonData));

        // ASSERT
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Invalid credentials provided"));

        verify(authService).login(any(AuthenticationWithCredentialsRequest.class));
    }
}
