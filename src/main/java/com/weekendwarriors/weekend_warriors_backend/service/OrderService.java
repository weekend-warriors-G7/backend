package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.calls.ImageManagement;
import com.weekendwarriors.weekend_warriors_backend.dto.OrderDTO;
import com.weekendwarriors.weekend_warriors_backend.dto.OrderedProduct;
import com.weekendwarriors.weekend_warriors_backend.exception.NotAuthenticated;
import com.weekendwarriors.weekend_warriors_backend.exception.UserNotFound;
import com.weekendwarriors.weekend_warriors_backend.model.Product;
import com.weekendwarriors.weekend_warriors_backend.model.User;
import com.weekendwarriors.weekend_warriors_backend.repository.OrderRepository;
import com.weekendwarriors.weekend_warriors_backend.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ImageManagement imageManagement;
    private final MongoTemplate mongoTemplate;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, ImageManagement imageManagement, MongoTemplate mongoTemplate) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.imageManagement = imageManagement;
        this.mongoTemplate = mongoTemplate;
    }

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

    public List<OrderedProduct> getTop10MostOrderedProducts() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("product.$id")
                        .count().as("orderCount"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "orderCount")),
                Aggregation.limit(10),
                Aggregation.lookup("products", "_id", "_id", "productDetails"),
                Aggregation.unwind("productDetails"),
                Aggregation.project("orderCount").and("productDetails").as("product")
        );

        return mongoTemplate.aggregate(aggregation, "orders", OrderedProduct.class)
                .getMappedResults()
                .stream()
                .map(orderedProduct -> {
                    try {
                        orderedProduct.getProduct().setImageId(imageManagement.getImageLink(orderedProduct.getProduct().getImageId()));
                        return orderedProduct;
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to fetch image link for product", e);
                    }
                })
                .toList();
    }
}
