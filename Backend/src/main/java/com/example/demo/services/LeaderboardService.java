package com.example.demo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.UserLeaderboardDto;
import com.example.demo.repositories.UserRepository;

@Service
public class LeaderboardService {
    
    public final UserRepository userRepository;
    
    public LeaderboardService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> getLeaderboard(){
        List<UserLeaderboardDto> leaderboard = userRepository.getLeaderboard();
        List<Map<String,Object>> leaderboardHash = new ArrayList<>();
        for(UserLeaderboardDto userInfoDto : leaderboard){
            leaderboardHash.add(userInfoDto.toMapForLeaderboard());
        }

        return ResponseEntity.ok().body(Map.of("data",leaderboardHash));
    }

}
