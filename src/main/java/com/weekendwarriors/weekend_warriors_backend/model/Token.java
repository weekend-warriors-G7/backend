package com.weekendwarriors.weekend_warriors_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.weekendwarriors.weekend_warriors_backend.enums.JWTTokenType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tokens")
public class Token {
    @Id
    private String id;

    private String token;

    private JWTTokenType type;

    private boolean expired;

    private boolean revoked;

    @DBRef
    @JsonIgnore
    private User user;

}
