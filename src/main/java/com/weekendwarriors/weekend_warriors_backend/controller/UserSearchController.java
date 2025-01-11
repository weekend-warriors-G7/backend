package com.weekendwarriors.weekend_warriors_backend.controller;

import com.weekendwarriors.weekend_warriors_backend.model.UserSearch;
import com.weekendwarriors.weekend_warriors_backend.service.SearchService;
import com.weekendwarriors.weekend_warriors_backend.service.UserSearchService;
import com.weekendwarriors.weekend_warriors_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/user-searches")
public class UserSearchController {

    private final UserSearchService userSearchService;

    @Autowired
    public UserSearchController(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @PostMapping
    public ResponseEntity<UserSearch> saveUserSearch(@RequestBody UserSearch userSearch) {
        UserSearch savedRelationship = userSearchService.saveUserSearch(userSearch);
        return ResponseEntity.ok(savedRelationship);
    }

    @GetMapping("")
    public ResponseEntity<List<UserSearch>> getAll() {
        List<UserSearch> searches = userSearchService.getAll();
        return ResponseEntity.ok(searches);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserSearch>> getSearchesByUserId(@PathVariable String userId) {
        List<UserSearch> searches = userSearchService.getSearchesByUserId(userId);
        return ResponseEntity.ok(searches);
    }

    @GetMapping("/search/{searchId}")
    public ResponseEntity<List<UserSearch>> getUsersBySearchId(@PathVariable String searchId) {
        List<UserSearch> users = userSearchService.getUsersBySearchId(searchId);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUserSearch(@RequestParam String userId, @RequestParam String searchId) {
        UserSearch userSearch = userSearchService.deleteUserSearch(userId, searchId);
        if (userSearch != null)
            return ResponseEntity.noContent().build();
        else
            return ResponseEntity.status(NOT_FOUND).body("No user or search with ids:" + userId + " " + searchId);
    }
}
