package com.example.demo.controllers.Rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.services.LeaderboardService;


@RestController
public class LeaderboardController {

    public final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService){
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        return leaderboardService.getLeaderboard();
    }
    
}
