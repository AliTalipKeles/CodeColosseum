package com.example.demo.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.dtos.GetProblemDto;
import com.example.demo.dtos.TestCaseDto;

@Repository
public class ProblemRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProblemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UUID createProblemRequest(
    String title,
    String statement,
    String input_format,
    String output_format,
    String constraints,
    String difficulty,
    UUID proposer_id)
    {

        UUID id = UUID.randomUUID();

        String sql = """
            INSERT INTO problems 
            (
            id,title, statement, input_format, output_format, constraints, difficulty, proposer_id)
            VALUES (?,?, ?, ?, ?, ?, ?::problem_difficulty, ?)
        """;

        jdbcTemplate.update(sql,
            id,
            title,
            statement,
            input_format,
            output_format,
            constraints,
            difficulty.toUpperCase(),
            proposer_id
        );

        return id;
    }
    
    public List<GetProblemDto> getProblems(String status,String difficulty){
        String sql = "SELECT id,title,statement,input_format,output_format,constraints,time_limit_s,memory_limit_mb,difficulty,status FROM problems WHERE 1=1 ";
        if("PENDING".equals(status) || "APPROVED".equals(status) || "REJECTED".equals(status)){
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
            problemDto.setStatement(rs.getString("statement"));
            problemDto.setInput_format(rs.getString("input_format"));
            problemDto.setOutput_format(rs.getString("output_format"));
            problemDto.setConstraints(rs.getString("constraints"));
            problemDto.setTime_limit_s(rs.getInt("time_limit_s"));
            problemDto.setMemory_limit_mb(rs.getInt("memory_limit_mb"));
            problemDto.setDifficulty(rs.getString("difficulty"));
            problemDto.setStatus(rs.getString("status"));
            return (problemDto);
        }
    );
        return problems;
    }


    public List<GetProblemDto> getProblem(UUID id){
        String sql = "SELECT id,title,statement,input_format,output_format,constraints,time_limit_s,memory_limit_mb,difficulty,status FROM problems WHERE id = ?";
        
        List<GetProblemDto> problems = jdbcTemplate.query(
        sql,
        (rs, rowNum) -> {
            GetProblemDto problemDto = new GetProblemDto();
            problemDto.setId(rs.getObject("id", UUID.class));
            problemDto.setTitle(rs.getString("title"));
            problemDto.setStatement(rs.getString("statement"));
            problemDto.setInput_format(rs.getString("input_format"));
            problemDto.setOutput_format(rs.getString("output_format"));
            problemDto.setConstraints(rs.getString("constraints"));
            problemDto.setTime_limit_s(rs.getInt("time_limit_s"));
            problemDto.setMemory_limit_mb(rs.getInt("memory_limit_mb"));
            problemDto.setDifficulty(rs.getString("difficulty"));
            problemDto.setStatus(rs.getString("status"));
            return (problemDto);
        },id
    );
        return problems;
    }


    public List<GetProblemDto> getAllProblems(){
        List<GetProblemDto> problems = jdbcTemplate.query(
        "SELECT id,title,statement,input_format,output_format,constraints,time_limit_s,memory_limit_mb,difficulty,status FROM problems",
        (rs, rowNum) -> {
            GetProblemDto problemDto = new GetProblemDto();
            problemDto.setId(rs.getObject("id", UUID.class));
            problemDto.setTitle(rs.getString("title"));
            problemDto.setStatement(rs.getString("statement"));
            problemDto.setInput_format(rs.getString("input_format"));
            problemDto.setOutput_format(rs.getString("output_format"));
            problemDto.setConstraints(rs.getString("constraints"));
            problemDto.setTime_limit_s(rs.getInt("time_limit_s"));
            problemDto.setMemory_limit_mb(rs.getInt("memory_limit_mb"));
            problemDto.setDifficulty(rs.getString("difficulty"));
            problemDto.setStatus(rs.getString("status"));
            return problemDto;
        }
    );
        return problems;
    }



    public void setProblemStatusApproved(UUID id,String title){
        String sql = "UPDATE problems SET status = 'APPROVED'::problem_status , reviewer_id = ?  WHERE title = ?";
        jdbcTemplate.update(sql,id,title);
    }

    public void setProblemStatusRejected(UUID id,String title){
        String sql = "UPDATE problems SET status = 'REJECTED'::problem_status , reviewer_id = ?  WHERE title = ?";
        jdbcTemplate.update(sql,id,title);
    }

    public void addTestCase(UUID problem_id, String stdin,String expected_stdout){
        String sql = "INSERT INTO test_cases (problem_id,stdin,expected_stdout) VALUES(?,?,?)";
        jdbcTemplate.update(sql,problem_id,stdin,expected_stdout);

    }

    public List<TestCaseDto> getTestCases(UUID id){
        String sql = "SELECT * FROM test_cases WHERE problem_id = ?";
        List<TestCaseDto> testCaseDtos = jdbcTemplate.query(
        sql,
        (rs, rowNum) -> {
            TestCaseDto dto = new TestCaseDto();
            dto.setId(rs.getObject("id", UUID.class));
            dto.setProblem_id(rs.getObject("problem_id",UUID.class));
            dto.setStdin(rs.getString("stdin"));
            dto.setExpected_stdout(rs.getString("expected_stdout"));

            return dto;
        },
        id
        );

        return testCaseDtos;
    }
    
    public List<TestCaseDto> getTestCases(UUID id, int limit){
        String sql = "SELECT * FROM test_cases WHERE problem_id = ? LIMIT "+limit;
        
        List<TestCaseDto> testCaseDtos = jdbcTemplate.query(
        sql,
        (rs, rowNum) -> {
            TestCaseDto dto = new TestCaseDto();
            dto.setId(rs.getObject("id", UUID.class));
            dto.setProblem_id(rs.getObject("problem_id",UUID.class));
            dto.setStdin(rs.getString("stdin"));
            dto.setExpected_stdout(rs.getString("expected_stdout"));

            return dto;
        },
        id
        );

        return testCaseDtos;
    }
}