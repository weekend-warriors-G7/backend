package com.weekendwarriors.weekend_warriors_backend.repository;

import com.weekendwarriors.weekend_warriors_backend.model.User;
import com.weekendwarriors.weekend_warriors_backend.model.UserSearch;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserSearchRepository extends MongoRepository<UserSearch, String>
{
    List<UserSearch> findByUserId(String userId);
    List<UserSearch> findBySearchId(String searchId);
}
