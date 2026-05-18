package com.example.demo.dtos;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchDto {
    private UUID match_id;
    private UUID contestant_a_id;
    private UUID contestant_b_id;
    private UUID problem_id;
    private Instant started_at;

    public MatchDto(UUID match_id ,UUID contestant_a_id,UUID contestant_b_id,UUID problem_id){
        this.contestant_a_id =contestant_a_id;
        this.contestant_b_id = contestant_b_id;
        this.match_id = match_id;
        this.problem_id = problem_id;
    }
    public UUID getOpponentId(UUID userId) {
        return contestant_a_id.equals(userId) ? contestant_b_id : contestant_a_id;
    }


}
