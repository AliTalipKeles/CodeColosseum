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
            String description = dto.getDescription();
            String input_format = dto.getInput_format();
            String output_format = dto.getOutput_format();
            String limits = dto.getLimits();
            String difficulty = dto.getDifficulty();

            repository.createProblemRequest(title,description,input_format,output_format,limits,difficulty,proposer_id);
            return ResponseEntity.ok().body(Map.of("result","Request sended"));
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("result","Problem request could not created"));
        }
    }

    public ResponseEntity<?> getPendingProblems(){
        try {
            return ResponseEntity.ok().body(repository.getPendingProblems());
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("result","Problem could not fetch"));
        }
    }

    public ResponseEntity<?> setPromblemStatusActive(UUID id,String title){
        try {
            repository.setProblemStatusActive(id,title);
            return ResponseEntity.ok().body(Map.of("result","Problem set active"));
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("result","Problem could not activated"));
        }
    }
    
}
