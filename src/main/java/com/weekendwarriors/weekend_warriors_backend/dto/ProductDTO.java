package com.weekendwarriors.weekend_warriors_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductDTO {
    private String name;
    private Double price;
    private String description;
    private String size;
    private String material;
    private String clothingType;
    private String colour;
    private String imageId;

    public ProductDTO(String name, Double price, String description, String size, String material, String clothingType, String colour, String imageId) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.size = size.toLowerCase();
        this.material = material.toLowerCase();
        this.clothingType = clothingType.toLowerCase();
        this.colour = colour.toLowerCase();
        this.imageId = imageId;
    }

    public ProductDTO(String name, Double price, String description, String size, String material, String clothingType, String colour)
    {
        this.name = name;
        this.price = price;
        this.description = description;
        this.size = size.toLowerCase();
        this.material = material.toLowerCase();
        this.clothingType = clothingType.toLowerCase();
        this.colour = colour.toLowerCase();
    }
}
