package com.weekendwarriors.weekend_warriors_backend.controller;

import com.weekendwarriors.weekend_warriors_backend.model.Search;
import com.weekendwarriors.weekend_warriors_backend.model.SearchDTO;
import com.weekendwarriors.weekend_warriors_backend.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/searches")
public class SearchController {

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    // Create or update a search
    @PostMapping
    public ResponseEntity<Search> saveSearch(@RequestBody SearchDTO search) {
        Search savedSearch = searchService.saveSearch(search);
        return ResponseEntity.ok(savedSearch);
    }

    @GetMapping
    public ResponseEntity<List<Search>> getAllSearches() {
        List<Search> searches = searchService.getAll();
        return ResponseEntity.ok(searches);
    }

    @GetMapping("/{text}")
    public ResponseEntity<Search> getSearchByText(@PathVariable String text) {
        Optional<Search> search = searchService.findByText(text);
        return search.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{text}")
    public ResponseEntity<Void> deleteSearch(@PathVariable String text) {
        searchService.deleteSearch(text);
        return ResponseEntity.noContent().build();
    }
}
