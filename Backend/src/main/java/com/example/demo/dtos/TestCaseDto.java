package com.example.demo.dtos;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TestCaseDto {
    private UUID id;
    private UUID problem_id;
    private String stdin;
    private String expected_stdout;
}
