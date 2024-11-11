package com.weekendwarriors.weekend_warriors_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductDTO {
    private String name;
    private Double price;
    private String description;
    private String size;
    private String material;
    private String clothingType;
    private String colour;
}
