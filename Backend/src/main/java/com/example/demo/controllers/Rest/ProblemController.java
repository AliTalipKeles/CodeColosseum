package com.example.demo.controllers.Rest;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.ProblemCreateDto;
import com.example.demo.services.ProblemService;
import com.example.demo.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;




@RestController
@RequestMapping("problem")
public class ProblemController {

    private final ProblemService service;

    public ProblemController(ProblemService service) {
        this.service = service;
    }
    
    @PostMapping("/createrequest")
    public ResponseEntity<?> createProblemRequest(HttpServletRequest request,@RequestBody ProblemCreateDto dto) {
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(401).body(Map.of("result","Please Login again"));
        }
        
        String token = authHeader.substring(7);
        Claims claim = JwtUtil.validateToken(token);
        
        
        if(claim == null){
            return ResponseEntity.status(401).body(Map.of("result","Your Authorization expried"));
        }

        UUID proposer_id = UUID.fromString(claim.getSubject());

        return service.createProblemRequest(proposer_id,dto);
    }
      
    @GetMapping("")
    public ResponseEntity<?> getProblems(
                @RequestParam(required = false) String id,
                @RequestParam(required = false) String status,
                @RequestParam(required = false) String difficulty,
                HttpServletRequest request){
        // auth kontrolün (senin mevcut koddan)
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(401).body(Map.of("result","Please Login again"));
        }

        String token = authHeader.substring(7);
        Claims claim = JwtUtil.validateToken(token);

        if(claim == null){
            return ResponseEntity.status(401).body(Map.of("result","Your Authorization expired"));
        }
        if(id == null){
            return service.getProblems(status, difficulty);
        }
        return service.getProblem(UUID.fromString(id));    
    }

    @PutMapping("setapproved/{title}")
    public ResponseEntity<?> setProblemApproved(@PathVariable String title,HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(401).body(Map.of("result","Please Login again"));
        }

        String token = authHeader.substring(7);
        Claims claim = JwtUtil.validateToken(token);

        if(claim == null){
            return ResponseEntity.status(401).body(Map.of("result","Your Authorization expired"));
        }

        if (!("ADMIN".equals(claim.get("role")))){
            return ResponseEntity.status(403).body(Map.of("result","Only admins can perform this operation."));
        }

        return service.setPromblemStatusApproved(UUID.fromString(claim.getSubject()),title);
    }

    @PutMapping("setrejected/{title}")
    public ResponseEntity<?> setProblemInactive(@PathVariable String title,HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(401).body(Map.of("result","Please Login again"));
        }

        String token = authHeader.substring(7);
        Claims claim = JwtUtil.validateToken(token);

        if(claim == null){
            return ResponseEntity.status(401).body(Map.of("result","Your Authorization expired"));
        }

        if (!("ADMIN".equals(claim.get("role")))){
            return ResponseEntity.status(403).body(Map.of("result","Only admins can perform this operation."));
        }

        return service.setProblemStatusRejected(UUID.fromString(claim.getSubject()),title);
    }

    @PostMapping("addtestcase")
    public ResponseEntity<?> addTestCase(HttpServletRequest request,@RequestBody Map<String,?> data) {
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(401).body(Map.of("result","Please Login again"));
        }

        String token = authHeader.substring(7);
        Claims claim = JwtUtil.validateToken(token);

        if(claim == null){
            return ResponseEntity.status(401).body(Map.of("result","Your Authorization expired"));
        }

        return service.addTestCase(data);
        
    }

    @GetMapping("getTestCases")
    public ResponseEntity<?> getTestCases(@RequestParam(required = true) String id) {
        try {
            UUID problem_id = UUID.fromString(id);
            return service.getTestCases(problem_id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("result","You should add parameter named \'id\'"));
        }
    }   

    
    
}
