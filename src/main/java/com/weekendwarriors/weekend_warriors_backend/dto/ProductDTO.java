package com.weekendwarriors.weekend_warriors_backend.dto;

import com.weekendwarriors.weekend_warriors_backend.enums.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductDTO {
    private String sellerId;
    private String name;
    private Double price;
    private String description;
    private String size;
    private String material;
    private String clothingType;
    private String colour;
    private String imageId;
    private ProductStatus status;

    public ProductDTO(String sellerId, String name, Double price, String description, String size, String material, String clothingType, String colour, String imageId, ProductStatus status) {
        this.sellerId = sellerId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.size = size.toLowerCase();
        this.material = material.toLowerCase();
        this.clothingType = clothingType.toLowerCase();
        this.colour = colour.toLowerCase();
        this.imageId = imageId;
        this.status = status;
    }

    public ProductDTO(String sellerId, String name, Double price, String description, String size, String material, String clothingType, String colour, ProductStatus status)
    {
        this.sellerId = sellerId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.size = size.toLowerCase();
        this.material = material.toLowerCase();
        this.clothingType = clothingType.toLowerCase();
        this.colour = colour.toLowerCase();
        this.status = status;
    }
}
