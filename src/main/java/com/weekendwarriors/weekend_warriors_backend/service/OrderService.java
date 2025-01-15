package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.calls.ImageManagement;
import com.weekendwarriors.weekend_warriors_backend.dto.OrderForBuyerDTO;
import com.weekendwarriors.weekend_warriors_backend.dto.OrderForSellerDTO;
import com.weekendwarriors.weekend_warriors_backend.dto.OrderedProduct;
import com.weekendwarriors.weekend_warriors_backend.exception.NotAuthenticated;
import com.weekendwarriors.weekend_warriors_backend.exception.ProductNotFound;
import com.weekendwarriors.weekend_warriors_backend.exception.UserNotFound;
import com.weekendwarriors.weekend_warriors_backend.model.Order;
import com.weekendwarriors.weekend_warriors_backend.model.Product;
import com.weekendwarriors.weekend_warriors_backend.model.User;
import com.weekendwarriors.weekend_warriors_backend.repository.OrderRepository;
import com.weekendwarriors.weekend_warriors_backend.repository.ProductRepository;
import com.weekendwarriors.weekend_warriors_backend.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ImageManagement imageManagement;
    private final MongoTemplate mongoTemplate;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, ImageManagement imageManagement, MongoTemplate mongoTemplate, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.imageManagement = imageManagement;
        this.mongoTemplate = mongoTemplate;
        this.productRepository = productRepository;
    }

    public List<OrderForBuyerDTO> getAllOrdersForCurrentUser() throws UserNotFound, NotAuthenticated {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFound("Invalid email"));
            return returnListOfOrderAsListOfOrderForBuyerDTOs(orderRepository.findByUserId(user.getId()));
        }
        throw new NotAuthenticated("Not authenticated");
    }

    public List<OrderForSellerDTO> getOrdersPlacedByOtherUsersForProductsSoldByCurrentUser() throws UserNotFound {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFound("Invalid email"));
            List<Order> orders = orderRepository.findByUserIdNot(user.getId());
            List<Order> filteredOrders = orders.stream()
                    .filter(order -> user.getId().equals(order.getProduct().getOwner_id()))
                    .toList();
            return returnListOfOrderAsListOfOrderForSellerDTOs(filteredOrders);
        }
        throw new NotAuthenticated("Not authenticated");
    }

    private List<OrderForBuyerDTO> returnListOfOrderAsListOfOrderForBuyerDTOs(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> userRepository.findById(order.getProduct().getOwner_id())
                        .map(owner -> {
                            try {
                                return new OrderForBuyerDTO(
                                        order.getId(),
                                        owner.getEmail(),
                                        new Product(
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
                               return null;
                            }
                        })
                        .stream())
                        .filter(Objects::nonNull)
                .toList();
    }

    private List<OrderForSellerDTO> returnListOfOrderAsListOfOrderForSellerDTOs(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> {
                    try {
                        return Stream.of(new OrderForSellerDTO(
                                order.getId(),
                                order.getUser().getEmail(),
                                new Product(
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
                        ));
                    } catch (IOException e) {
                        return Stream.empty();
                    }
                })
                .toList();
    }

    public void placeOrderForCurrentUser(String productId) throws UserNotFound, ProductNotFound {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFound("Invalid email"));
            Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFound("The product was not found"));
            orderRepository.insert(new Order(product, user));
        }
        else
            throw new NotAuthenticated("Not authenticated");
    }

    public List<OrderedProduct> getTopMostOrderedProducts(int n) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("product.$id")
                        .count().as("orderCount"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "orderCount")),
                Aggregation.limit(n),
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