package com.weekendwarriors.weekend_warriors_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StatisticsNumberDTO {
    private String keyword;
    private Integer number;
}
