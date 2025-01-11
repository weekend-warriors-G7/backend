package com.weekendwarriors.weekend_warriors_backend.service;

import com.weekendwarriors.weekend_warriors_backend.model.Search;
import com.weekendwarriors.weekend_warriors_backend.model.SearchDTO;
import com.weekendwarriors.weekend_warriors_backend.repository.SearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchService {
    private final SearchRepository searchRepository;

    @Autowired
    public SearchService(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    public Search saveSearch(SearchDTO search)
    {
        Search searchItem = new Search(search.getText().toLowerCase());
        List<Search> allSearches = searchRepository.findAll();
        boolean searchExists = false;
        for(Search searchedElement : allSearches)
        {
            if(searchedElement.getText() == null)
                this.searchRepository.deleteById(searchedElement.getId());
            else if(searchedElement.getText().equals(search.getText()))
                searchExists = true;
        }
        if(!searchExists)
            return this.searchRepository.save(searchItem);
        return null;
    }

    public Optional<Search> findByText(String text) {
        return this.searchRepository.findByText(text.toLowerCase());
    }

    public void deleteSearch(String text) {
        List<Search> allSearches = searchRepository.findAll();
        for(Search search : allSearches)
        {
            if(search.getText() == null)
                this.searchRepository.deleteById(search.getId());
            else if(search.getText().equals(text.toLowerCase()))
                this.searchRepository.deleteById(search.getId());
        }
    }

    public List<Search> getAll() {
        return searchRepository.findAll();
    }
}