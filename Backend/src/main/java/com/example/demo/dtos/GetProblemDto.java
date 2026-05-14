package com.example.demo.dtos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetProblemDto {
    private UUID id;
    private String title;
    private  String statement;
    private  String input_format;
    private  String output_format;
    private  String constraints;
    private  String difficulty;
    private int time_limit_s;
    private int memory_limit_mb;
    private String status;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("statement", statement);
        map.put("input_format", input_format);
        map.put("output_format", output_format);
        map.put("constraints", constraints);
        map.put("difficulty", difficulty);
        map.put("time_limit_s", time_limit_s);
        map.put("memory_limit_mb", memory_limit_mb);
        map.put("status", status);
        return map;
    }
}
