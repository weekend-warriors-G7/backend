package com.weekendwarriors.weekend_warriors_backend.dto;

import com.weekendwarriors.weekend_warriors_backend.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderedProduct {
    private Product product;
    private int orderCount;
}
