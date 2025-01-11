package com.weekendwarriors.weekend_warriors_backend.repository;

import com.weekendwarriors.weekend_warriors_backend.model.Product;
import com.weekendwarriors.weekend_warriors_backend.model.Search;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SearchRepository extends MongoRepository<Search, String>
{
    Optional<Search> findByText(String text);
}
