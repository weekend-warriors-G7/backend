package com.weekendwarriors.weekend_warriors_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    @DBRef
    private User user;

    @DBRef
    private Product product;

    private Date orderDate = new Date(System.currentTimeMillis());

    public Order(Product product, User user) {
        this.product = product;
        this.user = user;
    }
}