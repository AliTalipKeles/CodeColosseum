package com.example.demo.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.dtos.UserInfoDto;
import com.example.demo.dtos.UserLeaderboardDto;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void register(String username,String email,String password_hash){
        String sql = "INSERT INTO users (username,email,password_hash) Values(?,?,?)";
        jdbcTemplate.update(sql,username,email,password_hash);
    }

    public UserInfoDto getUserInfo(String username){
        try {
            String sql = "SELECT id,username,email,password_hash,role,created_at,elo_rating,total_matches FROM users Where username = ?"; 
            UserInfoDto userLoginDto = jdbcTemplate.queryForObject(sql, (rs,rowNum) -> {
            UserInfoDto dto = new UserInfoDto();
            dto.setId(rs.getObject("id", UUID.class));
            dto.setUsername(rs.getString("username"));
            dto.setPassword(rs.getString("password_hash"));
            dto.setRole(rs.getString("role"));
            dto.setAccount_create_date(rs.getTimestamp("created_at").toInstant());
            dto.setElo(rs.getInt("elo_rating"));
            dto.setTotal_matches(rs.getInt("total_matches"));
            dto.setEmail(rs.getString("email"));
            return dto;
        },username);
        return userLoginDto;
        } catch (DataAccessException e) {
            return null;
        }
    }
    public List<UserLeaderboardDto> getLeaderboard(){
        try {
    String sql = "SELECT username, elo_rating FROM users ORDER BY elo_rating DESC LIMIT 10";
    List<UserLeaderboardDto> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
        UserLeaderboardDto dto = new UserLeaderboardDto();
        dto.setUsername(rs.getString("username"));
        dto.setElo(rs.getInt("elo_rating"));
                return dto;
            });
            return list;
        } catch (DataAccessException e) {
            return null;
        }
    }

    public void deleteUser(String username){
        String sql = "DELETE FROM users Where username = ?";
        jdbcTemplate.update(sql, username);
    }

    public int getUserElo(String username) {
        String sql = "SELECT elo_rating FROM users WHERE username = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, username);
    }
}
