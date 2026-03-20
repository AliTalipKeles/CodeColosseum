package com.example.demo.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProblemCreateDto {
    public String title;
    public String description;
    public String inputFormat;
    public String outputFormat;
    public String limits;
    public String difficulty;
}

