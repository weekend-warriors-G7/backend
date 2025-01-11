package com.weekendwarriors.weekend_warriors_backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "user_searches")
public class UserSearch {

    @Id
    private String id;

    private String userId;
    private String searchId;

    public UserSearch(String userId, String searchId)
    {
        this.userId = userId;
        this.searchId = searchId;
        this.id = userId + "_" + searchId; // Generate composite key
    }
}