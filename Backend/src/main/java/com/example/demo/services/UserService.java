package com.example.demo.services;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.UserInfoDto;
import com.example.demo.repositories.UserRepository;
import com.example.demo.util.JwtUtil;

@Service
public class UserService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository repository, BCryptPasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public ResponseEntity<?> register(String username,String email,String password){
        try {
            repository.register(username, email ,encoder.encode(password));
            return ResponseEntity.ok(Map.of("result","Successful"));
        } catch (Exception e) {
            return ResponseEntity
                .badRequest()
                .body(Map.of("result","Username is already in use"));
        }
    }

    public ResponseEntity<?> login(String username,String password){
        try {
            UserInfoDto loginDto = repository.getUserInfo(username);

            if(loginDto == null){
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("result","Invalid username or password"));
            }

            if(encoder.matches(password, loginDto.getPassword())){ 
                String token = JwtUtil.generateToken(
                    loginDto.getId(),
                    loginDto.getUsername(),
                    loginDto.getRole()
                );
                return ResponseEntity.ok(Map.of("data", token));
            } else {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("result","Invalid username or password"));
            }

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("result","Invalid username or password"));
        }
    }

    public ResponseEntity<?> getUserInfo(String username){
        UserInfoDto dto = repository.getUserInfo(username);

        if(dto == null){
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("result","User not found"));
        }

        Map<String,Object> user = dto.toMapWithoutPassword();
        user.remove("email");
        user.remove("id");

        return ResponseEntity.ok().body(Map.of("data",user));
    }

    public ResponseEntity<?> deleteUser(String username){
        try {
            repository.deleteUser(username);
            return ResponseEntity.ok(Map.of("result","User deleted"));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("result","User deletion failed"));
        }
    }
}