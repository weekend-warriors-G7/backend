package com.weekendwarriors.weekend_warriors_backend.repository;

import com.weekendwarriors.weekend_warriors_backend.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String>
{
}
