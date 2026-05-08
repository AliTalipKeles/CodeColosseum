package com.example.demo.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProblemCreateDto {
    private String title;
    private  String statement;
    private  String input_format;
    private  String output_format;
    private  String constraints;
    private  String difficulty;
}

