package com.weekendwarriors.weekend_warriors_backend.repository;

import com.weekendwarriors.weekend_warriors_backend.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
    List<Order> findByUserIdNot(String userId);
}
