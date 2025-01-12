package com.weekendwarriors.weekend_warriors_backend.controller;

import com.stripe.model.checkout.Session;
import com.stripe.exception.StripeException;
import com.stripe.Stripe;
import com.stripe.net.RequestOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @GetMapping("/verify-session/{sessionId}")
    public ResponseEntity<?> verifyPaymentSession(@PathVariable String sessionId) {
        try {
            // Set your Stripe secret key
            Stripe.apiKey = stripeSecretKey;

            // Retrieve the session details from Stripe
            Session session = Session.retrieve(sessionId);

            // Check if the payment was successful
            if ("paid".equals(session.getPaymentStatus())) {
                return ResponseEntity.ok(new PaymentStatusResponse("success"));
            } else {
                return ResponseEntity.ok(new PaymentStatusResponse("failed"));
            }
        } catch (StripeException e) {
            return ResponseEntity.status(500).body("Error verifying payment");
        }
    }
}

class PaymentStatusResponse {
    private String status;

    public PaymentStatusResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

