package com.weekendwarriors.weekend_warriors_backend.controller;

import com.weekendwarriors.weekend_warriors_backend.dto.StatisticsListDTO;
import com.weekendwarriors.weekend_warriors_backend.dto.StatisticsNumberDTO;
import com.weekendwarriors.weekend_warriors_backend.dto.UserDTO;
import com.weekendwarriors.weekend_warriors_backend.model.Search;
import com.weekendwarriors.weekend_warriors_backend.model.UserSearch;
import com.weekendwarriors.weekend_warriors_backend.service.SearchService;
import com.weekendwarriors.weekend_warriors_backend.service.UserSearchService;
import com.weekendwarriors.weekend_warriors_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/statistics")
public class SearchStatisticsController
{
    private final UserSearchService userSearchService;
    private final SearchService searchService;
    private final UserService userService;

    @Autowired
    public SearchStatisticsController(UserSearchService userSearchService, UserService userService, SearchService searchService)
    {
        this.userSearchService = userSearchService;
        this.searchService = searchService;
        this.userService = userService;
    }

    @GetMapping("/find/{keyword}")
    public ResponseEntity<?> findUsersWhoCheckedThisKeyword(@PathVariable String keyword) throws IOException {
        try {
            boolean keywordExists = searchService.findByText(keyword).isPresent();
            if (keywordExists)
            {
                String keyWordId = searchService.findByText(keyword).get().getId();
                List<UserSearch> userIdsWhoSearchedWord = userSearchService.getUsersBySearchId(keyWordId);
                List<UserDTO> users = new LinkedList<>();
                for (UserSearch userId : userIdsWhoSearchedWord)
                {
                    UserDTO actualUser = new UserDTO();
                    actualUser = userService.getUserById(userId.getUserId());
                    users.add(actualUser);
                }
                return ResponseEntity.status(OK).body(users);
            } else {
                return ResponseEntity.status(NOT_FOUND).body("No such keyword exists yet");
            }
        } catch (IOException e) {
            return ResponseEntity.status(BAD_REQUEST).body("Error finding our users");
        }
    }

    @GetMapping("/find-number/{keyword}")
    public ResponseEntity<?> findNumberOfUsersWhoCheckedThisKeyword(@PathVariable String keyword) throws IOException {
        boolean keywordExists = searchService.findByText(keyword).isPresent();
        if (keywordExists)
        {
            String keyWordId = searchService.findByText(keyword).get().getId();
            List<UserSearch> userIdsWhoSearchedWord = userSearchService.getUsersBySearchId(keyWordId);
            int number = userIdsWhoSearchedWord.size();
            return ResponseEntity.status(OK).body(number);
        }
        else
        {
            return ResponseEntity.status(NOT_FOUND).body("No such keyword exists yet");
        }
    }

    @GetMapping("most-wanted-number")
    public ResponseEntity<?> getAllKeywordsInOrderOfHowViewedTheyAre() throws IOException {
        List<Search> allSearchedItems = searchService.getAll();
        Map<String, Integer> allKeywordsInOrderOfHowViewedTheyAre = new HashMap<>();

        for (Search searchedItem : allSearchedItems) {
            Integer count = userSearchService.getUsersBySearchId(searchedItem.getId()).size();
            allKeywordsInOrderOfHowViewedTheyAre.put(searchedItem.getText(), count);
        }

        List<StatisticsNumberDTO> sortedKeywords = allKeywordsInOrderOfHowViewedTheyAre.entrySet()
                .stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .map(entry -> new StatisticsNumberDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return ResponseEntity.status(OK).body(sortedKeywords);
    }

    @GetMapping("most-wanted-users")
    public ResponseEntity<?> getAllKeywordsInOrderOfHowViewedTheyAreUsers() throws IOException {
        try {
            List<Search> allSearchedItems = searchService.getAll();
            Map<String, List<UserDTO>> keywordsWithUsers = new HashMap<>();

            for (Search searchedItem : allSearchedItems) {
                List<UserSearch> userSearches = userSearchService.getUsersBySearchId(searchedItem.getId());

                List<UserDTO> users = new LinkedList<>();
                for (UserSearch userId : userSearches) {
                    UserDTO actualUser = userService.getUserById(userId.getUserId());
                    users.add(actualUser);
                }

                keywordsWithUsers.put(searchedItem.getText(), users);
            }

            List<StatisticsListDTO> statisticsList = keywordsWithUsers.entrySet()
                    .stream()
                    .sorted((entry1, entry2) -> Integer.compare(entry2.getValue().size(), entry1.getValue().size()))
                    .map(entry -> new StatisticsListDTO(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            return ResponseEntity.status(OK).body(statisticsList);
        } catch (IOException e) {
            return ResponseEntity.status(BAD_REQUEST).body("Error finding our users");
        }
    }

}
