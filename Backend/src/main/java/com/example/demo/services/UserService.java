package com.example.demo.services;

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

    public void register(String username,String email,String password_hash){
        repository.register(username, email ,encoder.encode(password_hash));
    }

    public String login(String username,String password){
        UserInfoDto loginDto = repository.getUserInfo(username);
        if(loginDto == null){
            return null;
        }

        if(encoder.matches(password, loginDto.getPassword())){ 
            return JwtUtil.generateToken(loginDto.getUUID(),loginDto.getUsername(),loginDto.getRole());
        }else{
            return null;
        }
    }

    public UserInfoDto getUserInfo(String username){
        return repository.getUserInfo(username);
    }

    public void deleteUser(String username){
        repository.deleteUser(username);
    }
}
