package com.weekendwarriors.weekend_warriors_backend.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.SubscriptionListParams;
import com.weekendwarriors.weekend_warriors_backend.dto.UserDTO;
import com.weekendwarriors.weekend_warriors_backend.exception.UserNotFound;
import com.weekendwarriors.weekend_warriors_backend.service.OrderService;
import com.weekendwarriors.weekend_warriors_backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.subscription.id}")
    private String subscriptionId;

    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public PaymentController(  OrderService orderService, UserService userService)
    {
        Stripe.apiKey = stripeSecretKey;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/verify-session/{sessionId}/{productId}")
    public ResponseEntity<?> verifyPaymentSession(@PathVariable String sessionId, @PathVariable String productId) {
        try {




            // Retrieve the session details from Stripe
            Session session = Session.retrieve(sessionId);

            // Check if the payment was successful
            if ("paid".equals(session.getPaymentStatus())) {
                orderService.placeOrderForCurrentUser(productId);
                return ResponseEntity.ok(new PaymentStatusResponse("success"));
            } else {
                return ResponseEntity.ok(new PaymentStatusResponse("failed"));
            }
        } catch (StripeException e) {
            return ResponseEntity.status(500).body("Error verifying payment");
        } catch (UserNotFound e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/verify-subscription/{sessionId}")
    public ResponseEntity<?> verifySubscription(@PathVariable String sessionId) throws StripeException {
        try {
        Session session = Session.retrieve(sessionId);

        // Ensure the session is valid and related to a subscription
        if (!"subscription".equals(session.getMode())) {
            return ResponseEntity.badRequest().body("Session is not for a subscription");
        }

        // Retrieve the subscription ID from the session
        String subscriptionId = session.getSubscription();
        if (subscriptionId == null) {
            return ResponseEntity.badRequest().body("No subscription found for this session");
        }

        // Retrieve the subscription details from Stripe
        Subscription subscription = Subscription.retrieve(subscriptionId);

        // Check the subscription status
        if ("active".equals(subscription.getStatus())) {
            return ResponseEntity.ok(new SubscriptionStatusResponse("active"));
        } else if ("incomplete".equals(subscription.getStatus())) {
            return ResponseEntity.ok(new SubscriptionStatusResponse("incomplete"));
        } else if ("past_due".equals(subscription.getStatus())) {
            return ResponseEntity.ok(new SubscriptionStatusResponse("past_due"));
        } else if ("canceled".equals(subscription.getStatus())) {
            return ResponseEntity.ok(new SubscriptionStatusResponse("canceled"));
        } else {
            return ResponseEntity.ok(new SubscriptionStatusResponse("unknown"));
        }
    } catch (StripeException e) {
        return ResponseEntity.status(500).body("Error verifying subscription");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("An unexpected error occurred");
    }

    }

    @GetMapping("/verify-subscription")
    public ResponseEntity<?> verifySubscription(HttpServletRequest request) throws StripeException {
        try {

            String token = userService.getJwtTokenFromRequest(request);
            UserDTO user = userService.getUser(token);

            if (user == null) {
                return ResponseEntity.status(404).body("User not found");
            }

            String stripeCustomerId = user.getStripeId();

            if (stripeCustomerId == null || stripeCustomerId.isEmpty()) {
                return ResponseEntity.status(404).body("User is not subscribed");
            }

            // List subscriptions for the customer
            SubscriptionListParams params = SubscriptionListParams.builder()
                    .setCustomer(stripeCustomerId)
                    .setLimit(100L) // Adjust the limit as needed
                    .build();

            List<Subscription> subscriptions = Subscription.list(params).getData();

            // Check if the customer has an active subscription with the given price ID
            boolean hasActiveSubscription = subscriptions.stream()
                    .anyMatch(subscription ->
                            "active".equals(subscription.getStatus()) &&
                                    subscription.getItems().getData().stream()
                                            .anyMatch(item -> subscriptionId.equals(item.getPrice().getId()))
                    );

            if (hasActiveSubscription) {
                return ResponseEntity.ok(new SubscriptionStatusResponse("active"));
            } else {
                return ResponseEntity.ok(new SubscriptionStatusResponse("inactive"));
            }
        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error verifying subscription");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An unexpected error occurred");
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

class SubscriptionStatusResponse {
    private String status;

    public SubscriptionStatusResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
}