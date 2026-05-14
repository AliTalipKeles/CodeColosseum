package com.example.demo.repositories;

import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.dtos.MatchDto;

@Repository
public class MatchRepository {
    
    private final JdbcTemplate jdbcTemplate;

    public MatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MatchDto createMatch(String id,String contestant_a_id ,String constestant_b_id,String difficulty_level){
        try {
            String create_match_sql = "INSERT INTO matches (id,contestant_a_id,contestant_b_id,problem_id) VALUES(?,?,?,?)";
            String sql = "SELECT id FROM problems WHERE difficulty = ?::problem_difficulty AND status = 'APPROVED' ORDER BY RANDOM() LIMIT 1";
            UUID problem_id = jdbcTemplate.queryForObject(
                sql,
                UUID.class,
                difficulty_level
            );
            jdbcTemplate.update(create_match_sql,UUID.fromString(id),UUID.fromString(contestant_a_id),UUID.fromString(constestant_b_id),problem_id);
        
            MatchDto matchDto = new MatchDto(UUID.fromString(id),UUID.fromString(contestant_a_id),UUID.fromString(constestant_b_id),problem_id);
            return matchDto;
        } catch (Exception e){
            return null;
        }
        
    }


    public MatchDto getMatch(UUID matchID){
        String sql = "SELECT id ,contestant_a_id,constestant_b_id,problem_id FROM matches WHERE id = ?";

        MatchDto matchDto = jdbcTemplate.queryForObject(sql,
            (rs,rowNum) -> {
            MatchDto dto = new MatchDto();
            dto.setMatch_id(matchID);
            dto.setContestant_a_id(rs.getObject("contestant_a_id",UUID.class));
            dto.setContestant_b_id(rs.getObject("contestant_b_id",UUID.class));
            dto.setProblem_id(rs.getObject("problem_id",UUID.class));

            return dto;
        });
        return matchDto;
    }
}
