package com.weekendwarriors.weekend_warriors_backend.repository;

import com.weekendwarriors.weekend_warriors_backend.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String>
{
}
