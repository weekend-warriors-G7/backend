package com.weekendwarriors.weekend_warriors_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.weekendwarriors.weekend_warriors_backend.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SearchDTO
{
    private String text;
}
