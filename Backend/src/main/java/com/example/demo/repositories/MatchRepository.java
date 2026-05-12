package com.example.demo.repositories;

import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MatchRepository {
    
    private final JdbcTemplate jdbcTemplate;

    public MatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String createMatch(String id,String contestant_a_id ,String constestant_b_id,String difficulty_level){
        try {
            String create_match_sql = "INSERT INTO matches (id,contestant_a_id,contestant_b_id,problem_id) VALUES(?,?,?,?)";
            String sql = "SELECT id FROM problems WHERE difficulty = ?::problem_difficulty AND status = 'APPROVED' ORDER BY RANDOM() LIMIT 1";
            UUID problem_id = jdbcTemplate.queryForObject(
                sql,
                UUID.class,
                difficulty_level
            );
            jdbcTemplate.update(create_match_sql,UUID.fromString(id),UUID.fromString(contestant_a_id),UUID.fromString(constestant_b_id),problem_id);
        

            return "Match is created";
        } catch (DataAccessException e) {
            return e.getMessage();
        } catch (Exception e){
            return e.getMessage();
        }
        
    }
}
