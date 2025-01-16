package com.weekendwarriors.weekend_warriors_backend.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.weekendwarriors.weekend_warriors_backend.calls.ImageManagement;
import com.weekendwarriors.weekend_warriors_backend.config.StripeConfig;
import com.weekendwarriors.weekend_warriors_backend.dto.UserDTO;
import com.weekendwarriors.weekend_warriors_backend.model.Product;
import com.weekendwarriors.weekend_warriors_backend.model.User;
import com.weekendwarriors.weekend_warriors_backend.service.ProductService;
import com.weekendwarriors.weekend_warriors_backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/checkout")
public class StripeController {

    private final ProductService productService;
    private final ImageManagement imageManagement;
    private final UserService userService;

    @Value("${stripe.subscription.id}")
    private String subscriptionId;

    @Autowired
    public StripeController(StripeConfig stripeConfig,UserService userService, ProductService productService, ImageManagement imageManagement) {
        this.productService = productService;
        this.imageManagement = imageManagement;
        this.userService = userService;

        Stripe.apiKey = stripeConfig.getSecretKey(); // Set Stripe API key
    }

    @PostMapping("/create-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody Map<String, Object> requestData) {
        try {
            // Extract product ID from the request
            String productId = (String) requestData.get("productId");

            // Fetch product details using the ID
            Product product = productService.getProductById(productId);
            if (product == null) {
                return ResponseEntity.status(404).body("Product not found");
            }

            // Calculate price in cents (Stripe uses smallest currency unit)
            Long amount = (long) (product.getPrice() * 100);
            System.out.println(imageManagement.getImageLink(product.getImageId()));
            // Create a Stripe Checkout Session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:3000/success?session_id={CHECKOUT_SESSION_ID}&product_id=" + productId)
                    .setCancelUrl("http://localhost:3000/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd") // Replace with product currency if needed
                                                    .setUnitAmount(amount) // Price in cents
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(product.getName())
                                                                    .setDescription(product.getDescription()) // Optional
                                                                    .addImage(product.getImageId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            // Return the session URL
            Map<String, String> response = new HashMap<>();
            response.put("url", session.getUrl());
            return ResponseEntity.ok(response);

        } catch (StripeException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to create session");
        }
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribeUser(HttpServletRequest request) {
        try {
            String token = userService.getJwtTokenFromRequest(request);
            UserDTO user = userService.getUser(token);

            if (user == null) {
                return ResponseEntity.status(404).body("User not found");
            }

            // Check if the user already has a Stripe customer ID
            String stripeCustomerId = user.getStripeId();

            if (stripeCustomerId == null || stripeCustomerId.isEmpty()) {
                // Create a new Stripe customer
                CustomerCreateParams customerParams = CustomerCreateParams.builder()
                        .setEmail(user.getEmail())
                        .setName(user.getFirstName() + " " + user.getLastName())
                        .build();

                Customer customer = Customer.create(customerParams);
                stripeCustomerId = customer.getId();

                // Update user with Stripe customer ID
                userService.updateUserStripeId(token, stripeCustomerId);
            }

            // Create a Stripe Checkout Session for subscription
            SessionCreateParams sessionParams = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION) // Subscription mode
                    .setSuccessUrl("http://localhost:3000/subscription-success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:3000/subscription-cancel")
                    .setCustomer(stripeCustomerId)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L) // Quantity of the subscription
                                    .setPrice(subscriptionId) // Use the Price ID for the subscription plan
                                    .build()
                    )
                    .build();

            Session session = Session.create(sessionParams);

            // Return the Checkout Session URL to the client
            Map<String, String> response = new HashMap<>();
            response.put("url", session.getUrl());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to create subscription session");
        }
    }




}
