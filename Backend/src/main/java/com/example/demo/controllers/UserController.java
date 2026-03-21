package com.example.demo.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.services.UserService;
import com.example.demo.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;



@RestController
@RequestMapping("user")
public class UserController {

    private final UserService service;

    public UserController(UserService service){
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> data) {
        return service.register(
            data.get("username"),
            data.get("email"),
            data.get("password")
        );
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> data){
        return service.login(
            data.get("username"),
            data.get("password")
        );
    }
    
    @GetMapping("/{username}")
    public ResponseEntity<?> getUser(HttpServletRequest request,@PathVariable String username) {

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(401).body(Map.of("result","Please Login again"));
        }

        String token = authHeader.substring(7);
        Claims claim = JwtUtil.validateToken(token);

        if(claim == null){
            return ResponseEntity.status(401).body(Map.of("result","Token expired"));
        }

        return service.getUserInfo(username);
    }
    
    @DeleteMapping("/delete/{username}")
    public ResponseEntity<?> deleteUser(HttpServletRequest request , @PathVariable String username){

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(401).body(Map.of("result","Please Login again"));
        }

        String token = authHeader.substring(7);
        Claims claim = JwtUtil.validateToken(token);

        if(claim == null){
            return ResponseEntity.status(401).body(Map.of("result","Your Authorization expired"));
        }

        if (!claim.get("role").equals("ADMIN")){
            return ResponseEntity.status(403).body(Map.of("result","Only admins can perform this operation."));
        }

        return service.deleteUser(username);
    }
    @GetMapping("/me")
    public ResponseEntity<?> userInfo(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(401).body(Map.of("result","Please Login again"));
        }
        
        String token = authHeader.substring(7);
        Claims claim = JwtUtil.validateToken(token);

        if(claim == null){
            return ResponseEntity.status(200).body(Map.of("result","Your Authorization expried"));
        }

        String username = (String)claim.get("username");

        return service.getUserInfo(username);
    }
}
