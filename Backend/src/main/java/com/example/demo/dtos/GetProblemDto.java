package com.example.demo.dtos;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetProblemDto {
    private UUID id;
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
