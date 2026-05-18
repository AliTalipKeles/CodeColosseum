package com.example.demo.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.demo.repositories.RatingRepository;
import com.example.demo.repositories.UserRepository;

@Service
public class RatingService {

    UserRepository userRepository;
    RatingRepository ratingRepository;
    private final int K = 32;
    
    public RatingService(UserRepository userRepository,RatingRepository ratingRepository){
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    public int calculateRatingChange(UUID user1, UUID user2,UUID matchId, int matchResult) {
    int elo1 = userRepository.getUserElo(user1);
    int elo2 = userRepository.getUserElo(user2);

    // Expected scores
    double expected1 = 1.0 / (1 + Math.pow(10, (elo2 - elo1) / 400.0));
    double expected2 = 1.0 - expected1;

    // Actual scores: matchResult = 1 (user1 win), 0 (draw), -1 (user1 loss)
    double score1, score2;
    if (matchResult == 1) {
        score1 = 1.0; score2 = 0.0;
    } else if (matchResult == 0) {
        score1 = 0.5; score2 = 0.5;
    } else {
        score1 = 0.0; score2 = 1.0;
    }

    // New ratings
    int newElo1 = (int) Math.max(0, Math.round(elo1 + K * (score1 - expected1)));
    int newElo2 = (int) Math.max(0, Math.round(elo2 + K * (score2 - expected2)));

    ratingRepository.createRatingHistory(user1, matchId, elo1, newElo1);
    ratingRepository.createRatingHistory(user2, matchId, elo2, newElo2);
    ratingRepository.updateUserElo(user1, newElo1);
    ratingRepository.updateUserElo(user2, newElo2);

    return newElo1 - elo1;
    }
    
}
