package com.example.demo.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetProblemDto {
    private String title;
    private  String description;
    private  String input_format;
    private  String output_format;
    private  String limits;
    private  String difficulty;
    private int time_limit_s;
    private int memory_limit_mb;
    private String status;
}
