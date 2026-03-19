package com.example.demo.repositories;

import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.dtos.UserInfoDto;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void register(String username,String email,String password_hash){
        String sql = "INSERT INTO \"user\" (username,email,password_hash) Values(?,?,?)";
        jdbcTemplate.update(sql,username,email,password_hash);
    }

    public UserInfoDto getUserInfo(String username){
        try {
            String sql = "SELECT id,username,email,password_hash,role,created_at,elo_rating,total_matches FROM \"user\" Where username = ?"; 
            UserInfoDto userLoginDto = jdbcTemplate.queryForObject(sql, (rs,rowNum) -> {
            UserInfoDto dto = new UserInfoDto();
            dto.setUUID(rs.getObject("id", UUID.class));
            dto.setUsername(rs.getString("username"));
            dto.setPassword(rs.getString("password_hash"));
            dto.setRole(rs.getString("role"));
            dto.setAccountCreateDate(rs.getTimestamp("created_at").toInstant());
            dto.setRank(rs.getInt("elo_rating"));
            dto.setTotalMatches(rs.getInt("total_matches"));
            dto.setEmail(rs.getString("email"));
            return dto;
        },username);
        return userLoginDto;
        } catch (DataAccessException e) {
            return null;
        }
        
    }

    public void deleteUser(String username){
        String sql = "DELETE FROM \"user\" Where username = ?";
        jdbcTemplate.update(sql, username);
    }
}
