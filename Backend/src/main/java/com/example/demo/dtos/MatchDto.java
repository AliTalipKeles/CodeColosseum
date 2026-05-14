package com.example.demo.dtos;

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

    public UUID getOpponentId(UUID userId) {
        return contestant_a_id.equals(userId) ? contestant_b_id : contestant_a_id;
    }


}
