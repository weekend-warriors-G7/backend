package com.weekendwarriors.weekend_warriors_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.stream.Collectors;

@Document(collection = "products")
public class Product
{
    @Id
    private String id;
    private String name;
    private Double price;
    private String description;
    private List<String> tags;

    // Constructors
    public Product() {}

    public Product(String name, Double price, String description, List<String> tags)
    {
        this.name = name;
        this.price = price;
        this.description = description;
        this.tags = tags;
        setTags(tags);
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
    public String getDescription() { return description; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setPrice(Double price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags)
    {
        if (tags != null)
        {
            this.tags = tags.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        }
        else
        {
            this.tags = null;
        }
    }
}
