package com.example.demo.repositories;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RatingRepository {
    
    private final JdbcTemplate jdbcTemplate;

    public RatingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createRatingHistory(UUID userId,UUID matchId,int elo_before,int elo_after){
        String sql = "INSERT INTO rating_history (user_id,match_id,rating_before,rating_after) Values(?,?,?,?)";
        jdbcTemplate.update(sql,userId,matchId,elo_before,elo_after);
    }

    public void updateUserElo(UUID userId,int new_elo){
        String sql = "UPDATE users SET elo_rating =  ? WHERE id = ?";
        jdbcTemplate.update(sql,new_elo,userId);
    }
    

}
