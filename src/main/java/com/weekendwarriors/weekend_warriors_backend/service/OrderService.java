package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.calls.ImageManagement;
import com.weekendwarriors.weekend_warriors_backend.dto.OrderDTO;
import com.weekendwarriors.weekend_warriors_backend.exception.NotAuthenticated;
import com.weekendwarriors.weekend_warriors_backend.exception.UserNotFound;
import com.weekendwarriors.weekend_warriors_backend.model.Product;
import com.weekendwarriors.weekend_warriors_backend.model.User;
import com.weekendwarriors.weekend_warriors_backend.repository.OrderRepository;
import com.weekendwarriors.weekend_warriors_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ImageManagement imageManagement;

    public List<OrderDTO> getAllOrdersForCurrentUser() throws UserNotFound, NotAuthenticated {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFound("Invalid email"));
            return orderRepository.findByUserId(user.getId()).stream().map(order -> {
                try {
                    return new OrderDTO(
                            order.getId(),
                            new Product
                            (
                                    order.getProduct().getId(),
                                    order.getProduct().getOwner_id(),
                                    order.getProduct().getName(),
                                    order.getProduct().getPrice(),
                                    order.getProduct().getDescription(),
                                    order.getProduct().getSize(),
                                    order.getProduct().getMaterial(),
                                    order.getProduct().getClothingType(),
                                    order.getProduct().getColour(),
                                    imageManagement.getImageLink(order.getProduct().getImageId()),
                                    order.getProduct().getStatus()
                            ),
                            order.getOrderDate()
                            );
                } catch (IOException e) {
                    throw new RuntimeException("Failed to process order due to image link retrieval issue", e);
                }
            }).toList();
        }
        throw new NotAuthenticated("Not authenticated");
    }
}
