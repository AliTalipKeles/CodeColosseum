package com.example.demo.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.dtos.GetProblemDto;

@Repository
public class ProblemRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProblemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createProblemRequest(
    String title,
    String description,
    String input_format,
    String output_format,
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
            input_format,
            output_format,
            limits,
            difficulty.toUpperCase(),
            proposer_id
        );
    }
    
    public List<GetProblemDto> getProblems(String status,String difficulty){
        String sql = "SELECT id,title,description,input_format,output_format,limits,time_limit_s,memory_limit_mb,difficulty,status FROM problems WHERE 1=1 ";
        if("PENDING".equals(status) || "ACTIVE".equals(status) || "INACTIVE".equals(status)){
            sql = sql.concat("AND status = \'"+status+"\'");
        }
        if("EASY".equals(difficulty) || "MEDIUM".equals(difficulty) ||"HARD".equals(difficulty)){
            sql = sql.concat("AND difficulty = \'"+difficulty+"\'");
           
        }
        
        List<GetProblemDto> problems = jdbcTemplate.query(
         sql,
        (rs, rowNum) -> {
            GetProblemDto problemDto = new GetProblemDto();
            problemDto.setId(rs.getObject("id", UUID.class));
            problemDto.setTitle(rs.getString("title"));
            problemDto.setDescription(rs.getString("description"));
            problemDto.setInput_format(rs.getString("input_format"));
            problemDto.setOutput_format(rs.getString("output_format"));
            problemDto.setLimits(rs.getString("limits"));
            problemDto.setTime_limit_s(rs.getInt("time_limit_s"));
            problemDto.setMemory_limit_mb(rs.getInt("memory_limit_mb"));
            problemDto.setDifficulty(rs.getString("difficulty"));
            problemDto.setStatus(rs.getString("status"));
            return problemDto;
        }
    );
        return problems;
    }

    public List<GetProblemDto> getAllProblems(){
        List<GetProblemDto> problems = jdbcTemplate.query(
        "SELECT id,title,description,input_format,output_format,limits,time_limit_s,memory_limit_mb,difficulty,status FROM problems",
        (rs, rowNum) -> {
            GetProblemDto problemDto = new GetProblemDto();
            problemDto.setId(rs.getObject("id", UUID.class));
            problemDto.setTitle(rs.getString("title"));
            problemDto.setDescription(rs.getString("description"));
            problemDto.setInput_format(rs.getString("input_format"));
            problemDto.setOutput_format(rs.getString("output_format"));
            problemDto.setLimits(rs.getString("limits"));
            problemDto.setTime_limit_s(rs.getInt("time_limit_s"));
            problemDto.setMemory_limit_mb(rs.getInt("memory_limit_mb"));
            problemDto.setDifficulty(rs.getString("difficulty"));
            problemDto.setStatus(rs.getString("status"));
            return problemDto;
        }
    );
        return problems;
    }

    public void setProblemStatusActive(UUID id,String title){
        String sql = "UPDATE problems SET status = 'ACTIVE'::problem_status , reviewer_id = ?  WHERE title = ?";
        jdbcTemplate.update(sql,id,title);
    }

    public void setProblemStatusInactive(UUID id,String title){
        String sql = "UPDATE problems SET status = 'INACTIVE'::problem_status , reviewer_id = ?  WHERE title = ?";
        jdbcTemplate.update(sql,id,title);
    }

    

}