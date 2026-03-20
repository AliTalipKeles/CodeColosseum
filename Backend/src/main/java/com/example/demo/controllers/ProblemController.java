package com.example.demo.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.ProblemCreateDto;
import com.example.demo.services.ProblemService;
import com.example.demo.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;


@RestController
public class ProblemController {

    private final ProblemService service;

    public ProblemController(ProblemService service) {
        this.service = service;
    }

    @PostMapping("problem/createrequest")
    public ResponseEntity<?> createProblemRequest(HttpServletRequest request,@RequestBody ProblemCreateDto dto) {
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(401).body(Map.of("result","Please Login again"));
        }
        
        String token = authHeader.substring(7);
        Claims claim = JwtUtil.validateToken(token);
        
        
        if(claim == null){
            return ResponseEntity.status(200).body(Map.of("result","Your Authorization expried"));
        }

        UUID proposer_id = UUID.fromString(claim.getSubject());

        return service.createProblemRequest(proposer_id,dto);
    }
    
}
