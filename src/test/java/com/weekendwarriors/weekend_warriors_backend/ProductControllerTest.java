package com.weekendwarriors.weekend_warriors_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weekendwarriors.weekend_warriors_backend.dto.ProductDTO;
import com.weekendwarriors.weekend_warriors_backend.enums.ProductStatus;
import com.weekendwarriors.weekend_warriors_backend.exception.InvalidToken;
import com.weekendwarriors.weekend_warriors_backend.model.Product;
import com.weekendwarriors.weekend_warriors_backend.service.ProductService;
import com.weekendwarriors.weekend_warriors_backend.service.UserService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private AutoCloseable mocks;

    @MockBean
    private ProductService productService;

    @MockBean
    private UserService userService;

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

    public String obtainToken() throws Exception
    {
        // Register a user
        String registerJson = """
                {
                    "email": "testuser@test.com",
                    "password": "TestPassword123!",
                    "firstName": "Test",
                    "lastName": "User"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson));

        // Login to get the token
        String loginJson = """
                {
                    "email": "testuser@test.com",
                    "password": "TestPassword123!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        // Extract token from response
        String responseBody = loginResult.getResponse().getContentAsString();
        String token = new ObjectMapper().readTree(responseBody).get("token").get("accessToken").asText();

        return token;
    }

    @Test
    public void getAllProducts_validRequest_returnsProductList() throws Exception {
        // ARRANGE
        String token = obtainToken();
        Product product = new Product("1","1", "Blue shirt", 24.9, "Blue cotton shirt", "M", "Cotton", "Casual", "Blue", "imageId1", ProductStatus.PENDING);
        Mockito.when(productService.getAllProducts()).thenReturn(Collections.singletonList(product));

        // ACT
        ResultActions result = mockMvc.perform(get("/products/all")
                .header("Authorization", "Bearer " + token));

        // ASSERT
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Blue shirt"));

        Mockito.verify(productService).getAllProducts();
    }

    @Test
    public void getProductById_validId_returnsProduct() throws Exception {
        // ARRANGE
        String token = obtainToken();
        Product product = new Product("1","1", "Blue shirt", 24.9, "Blue cotton shirt", "M", "Cotton", "Casual", "Blue", "imageId1", ProductStatus.PENDING);
        Mockito.when(productService.getProductById("1")).thenReturn(product);

        // ACT
        ResultActions result = mockMvc.perform(get("/products/1")
                .header("Authorization", "Bearer " + token));

        // ASSERT
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Blue shirt"));

        Mockito.verify(productService).getProductById("1");
    }
}
