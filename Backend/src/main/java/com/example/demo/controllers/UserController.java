package com.example.demo.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.UserInfoDto;
import com.example.demo.services.UserService;
import com.example.demo.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



@RestController
public class UserController {

    private final UserService service;

    public UserController(UserService service){
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> data) {

        String username = data.get("username");
        String email = data.get("email");
        String password = data.get("password");
        try {
            service.register(username, email , password);
            return ResponseEntity.status(200).body(Map.of("result","Succesful"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("result","Username is already in use"));
        }   
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> data,HttpServletResponse response){
        try {
            String token = service.login(
            data.get("username"),
            data.get("password")
        );
        
        return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED).body(Map.of("result","Invalid username or password"));
        }
    }
    
    @GetMapping("user/{username}")
    public ResponseEntity<Map<String,Object>> getUser(HttpServletRequest request,@PathVariable String username) {
         String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(401).body(Map.of("result","Please Login again"));
        }
        
        String token = authHeader.substring(7);
        Claims claim = JwtUtil.validateToken(token);

        if(claim != null){
            UserInfoDto dto = service.getUserInfo(username);
            if (dto == null) {
                return ResponseEntity.status(404).body(Map.of("result","User not found"));
            }
            Map<String,Object> user_info = dto.toMapWithoutPassword();
            user_info.remove("email");
            user_info.remove("id");

            return ResponseEntity.ok().body(user_info);
        }else{
            return  ResponseEntity.status(401).body(Map.of("result","Token expired"));
        }
    }
    
    @DeleteMapping("/user/delete/{username}")
    public ResponseEntity<Map<String,String>> deleteUser(HttpServletRequest request , @PathVariable String username){
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(401).body(Map.of("result","Please Login again"));
        }
        String token = authHeader.substring(7);
        Claims claim = JwtUtil.validateToken(token);

        if(claim == null){
            return ResponseEntity.status(200).body(Map.of("result","Your Authorization expried"));
        }
        
        if (claim.get("role").equals("ADMIN"))
            try{
                service.deleteUser(username);
                return ResponseEntity.status(200).body(Map.of("result","User deleted"));
            }catch(Exception e){
                return ResponseEntity.status(401).body(Map.of("result","The user deletion process could not be completed."));
            }
        else{
             return ResponseEntity.status(401).body(Map.of("result","Only admins can perform this operation."));
        }
    }
    @GetMapping("/me")
    public ResponseEntity<Map<String,Object>> userInfo(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.status(401).body(Map.of("result","Please Login again"));
        }
        
        String token = authHeader.substring(7);
        Claims claim = JwtUtil.validateToken(token);

        String username = (String)claim.get("username");

        UserInfoDto dto = service.getUserInfo(username);

        return ResponseEntity.ok().body(dto.toMapWithoutPassword());
    }
}
