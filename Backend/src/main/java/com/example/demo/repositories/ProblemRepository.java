package com.example.demo.repositories;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProblemRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProblemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createProblemRequest(
    String title,
    String description,
    String inputFormat,
    String outputFormat,
    String limits,
    String difficulty,
    UUID proposer_id)
    {
        String sql = """
            INSERT INTO problems 
            (title, description, input_format, output_format, limits, difficulty, proposer_id)
            VALUES (?, ?, ?, ?, ?, ?::problem_difficulty, ?)
        """;

        jdbcTemplate.update(sql,
            title,
            description,
            inputFormat,
            outputFormat,
            limits,
            difficulty.toUpperCase(),
            proposer_id
        );
    }

}