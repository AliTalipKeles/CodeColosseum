package com.example.demo.services;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.ProblemCreateDto;
import com.example.demo.repositories.ProblemRepository;
@Service
public class ProblemService {
    private final ProblemRepository repository;

    public ProblemService(ProblemRepository repository) {
        this.repository = repository;
    }

    public ResponseEntity<?> createProblemRequest(UUID proposer_id,ProblemCreateDto dto){
        try {
            String title = dto.getTitle();
            String statement = dto.getStatement();
            String input_format = dto.getInput_format();
            String output_format = dto.getOutput_format();
            String constraints = dto.getConstraints();
            String difficulty = dto.getDifficulty();

            UUID id = repository.createProblemRequest(title,statement,input_format,output_format,constraints,difficulty,proposer_id);
            return ResponseEntity.ok().body(Map.of("data",id));
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("result","Problem request could not created"));
        }
    }

    public ResponseEntity<?> setPromblemStatusApproved(UUID id,String title){
        try {
            repository.setProblemStatusApproved(id,title);
            return ResponseEntity.ok().body(Map.of("result","Problem set Approved"));
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("result","Problem could not approved"));
        }
    }

    public ResponseEntity<?> setProblemStatusRejected(UUID id,String title){
        try {
            repository.setProblemStatusRejected(id,title);
            return ResponseEntity.ok().body(Map.of("result","Problem set Rejected"));
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("result","Problem could not Rejected"));
        }
    }

    public ResponseEntity<?> getProblems(String status,String difficulty){
        try {
            
            return ResponseEntity.ok().body(Map.of("data",repository.getProblems(status, difficulty)));
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("result","Problem could not fetch"));
        }
    }

    public ResponseEntity<?> getProblem(UUID id){
        try {
            return ResponseEntity.ok().body(Map.of("data",repository.getProblem(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("result","Problem could not fetch"));
        }
    }
    
    public ResponseEntity<?>addTestCase(Map<String,?> data){
        try{
            UUID id = UUID.fromString((String)data.get("problem_id"));
            String stdin = (String) data.get("stdin");
            String expected_stdout = (String) data.get("expected_stdout");
            
            repository.addTestCase(id,stdin,expected_stdout);

            return ResponseEntity.ok().body(Map.of("result","Test case Added"));
        }catch(Exception e){
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("result","Test case could not added"));
        }
    }

    public ResponseEntity<?> getTestCases(UUID id){
        try{
            return ResponseEntity.ok().body(Map.of("data",repository.getTestCases(id)));
        }catch(Exception e){
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("result","Test cases could not fetch"));
        }

    }

    
}
