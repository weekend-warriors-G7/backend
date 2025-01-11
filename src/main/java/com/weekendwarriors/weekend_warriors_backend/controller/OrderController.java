package com.weekendwarriors.weekend_warriors_backend.controller;

import com.weekendwarriors.weekend_warriors_backend.dto.OrderForBuyerDTO;
import com.weekendwarriors.weekend_warriors_backend.dto.OrderForSellerDTO;
import com.weekendwarriors.weekend_warriors_backend.dto.OrderedProduct;
import com.weekendwarriors.weekend_warriors_backend.exception.ProductNotFound;
import com.weekendwarriors.weekend_warriors_backend.exception.UserNotFound;
import com.weekendwarriors.weekend_warriors_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/buyer")
    public ResponseEntity<List<OrderForBuyerDTO>> getOrdersForUser() throws UserNotFound {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getAllOrdersForCurrentUser());
    }

    @GetMapping("/top")
    public ResponseEntity<List<OrderedProduct>> getTopOrderedProducts(@RequestParam(defaultValue = "10") int n) {
        return ResponseEntity.status(HttpStatus.OK).body(this.orderService.getTopMostOrderedProducts(n));
    }

    @GetMapping("/seller")
    public ResponseEntity<List<OrderForSellerDTO>> getSoldOrderedProducts() throws UserNotFound {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getOrdersPlacedByOtherUsersForProductsSoldByCurrentUser());
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Map<String, String>> placeOrder(@PathVariable String productId) throws UserNotFound, ProductNotFound {
        Map<String, String> responseBody = new HashMap<>();
        orderService.placeOrderForCurrentUser(productId);
        responseBody.put("message", "Successful order placement");
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }
}
