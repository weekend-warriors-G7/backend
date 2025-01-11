package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.model.UserSearch;
import com.weekendwarriors.weekend_warriors_backend.repository.UserSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserSearchService
{
    private final UserSearchRepository userSearchRepository;

    @Autowired
    public UserSearchService(UserSearchRepository userSearchRepository) {
        this.userSearchRepository = userSearchRepository;
    }

    public List<UserSearch> getAll()
    {
        return userSearchRepository.findAll();
    }


    public UserSearch saveUserSearch(UserSearch userSearch)
    {
        boolean potentialExistingSearch = userSearchRepository.existsById(userSearch.getId());
        if(!potentialExistingSearch)
            return userSearchRepository.save(userSearch);
        return null;
    }

    public List<UserSearch> getSearchesByUserId(String userId) {
        return userSearchRepository.findByUserId(userId);
    }

    public List<UserSearch> getUsersBySearchId(String searchId) {
        return userSearchRepository.findBySearchId(searchId);
    }

    public UserSearch deleteUserSearch(String userId, String searchId)
    {
        String userSearchId = userId + "_" + searchId;

        boolean potentialExistingSearch = userSearchRepository.existsById(userSearchId);
        if(potentialExistingSearch)
        {
            UserSearch userSearch = userSearchRepository.findById(userSearchId).get();
            userSearchRepository.deleteById(userSearchId);
            return userSearch;
        }
        return null;
    }
}
