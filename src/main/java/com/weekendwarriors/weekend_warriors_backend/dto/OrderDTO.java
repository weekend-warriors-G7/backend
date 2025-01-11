package com.weekendwarriors.weekend_warriors_backend.dto;

import com.weekendwarriors.weekend_warriors_backend.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class OrderDTO {
    private String id;
    private Product product;
    private Date orderDate;
}
