package com.weekendwarriors.weekend_warriors_backend.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.weekendwarriors.weekend_warriors_backend.config.StripeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class StripeWebhookController {

    private final StripeConfig stripeConfig;
   // private final OrderService orderService;

//    @Autowired
//    public StripeWebhookController(StripeConfig stripeConfig, OrderService orderService) {
//        this.stripeConfig = stripeConfig;
//        this.orderService = orderService;
//    }

    @Autowired
    public StripeWebhookController(StripeConfig stripeConfig) {
        this.stripeConfig = stripeConfig;

    }

    @PostMapping("/stripe")
    public ResponseEntity<?> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            String endpointSecret = stripeConfig.getWebhookSecret();

            Event event = Webhook.constructEvent(
                    payload, sigHeader, endpointSecret
            );

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

                if (session != null) {
                    String sessionId = session.getId();

                }
            }

            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Invalid signature");
        }
    }
}
