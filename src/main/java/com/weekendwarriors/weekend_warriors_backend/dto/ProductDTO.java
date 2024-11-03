package com.weekendwarriors.weekend_warriors_backend.dto;

import java.util.List;

public class ProductDTO {
    private String name;
    private Double price;
    private String description;
    private List<String> tags;

    // Constructors
    public ProductDTO() {}

    public ProductDTO(String name, Double price, String description, List<String> tags) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.tags = tags;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
