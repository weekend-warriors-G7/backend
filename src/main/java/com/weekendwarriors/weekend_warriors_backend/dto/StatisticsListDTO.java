package com.weekendwarriors.weekend_warriors_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StatisticsListDTO {
    private String keyword;
    private List<UserDTO> users;
}

