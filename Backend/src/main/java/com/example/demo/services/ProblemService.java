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
            String title = dto.title;
            String description = dto.description;
            String inputFormat = dto.inputFormat;
            String outputFormat = dto.outputFormat;
            String limits = dto.limits;
            String difficulty = dto.difficulty;

            repository.createProblemRequest(title,description,inputFormat,outputFormat,limits,difficulty,proposer_id);
            return ResponseEntity.ok().body(Map.of("result","Request sended"));
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("result","Problem request is not created"));
        }

        
    }
    
}
