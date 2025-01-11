package com.weekendwarriors.weekend_warriors_backend.controller;

import com.weekendwarriors.weekend_warriors_backend.dto.OrderDTO;
import com.weekendwarriors.weekend_warriors_backend.dto.OrderedProduct;
import com.weekendwarriors.weekend_warriors_backend.exception.UserNotFound;
import com.weekendwarriors.weekend_warriors_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("")
    public ResponseEntity<List<OrderDTO>> getOrdersForUser() throws UserNotFound {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getAllOrdersForCurrentUser());
    }

    @GetMapping("/top-10")
    public ResponseEntity<List<OrderedProduct>> getTop10OrderedProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(this.orderService.getTop10MostOrderedProducts());
    }
}
