package com.weekendwarriors.weekend_warriors_backend.model;

import com.weekendwarriors.weekend_warriors_backend.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
@Getter
@Setter
public class Product
{
    @Id
    private String id;
    private String owner_id;
    private String name;
    private Double price;
    private String description;
    private String size;
    private String material;
    private String clothingType;
    private String colour;
    private String imageId;
    private ProductStatus status;

    public Product(String owner_id, String name, Double price, String description, String size, String material, String clothingType, String colour, String imageId, ProductStatus status)
    {
        this.owner_id = owner_id;
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
}
