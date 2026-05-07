package com.example.demo.dtos;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserLeaderboardDto {

    private String username;
    private int elo;

    public Map<String,Object> toMapForLeaderboard(){
        Map<String,Object> map = new HashMap<>();

        map.put("username", this.username);
        map.put("elo" , this.elo);

        return map;
    }
}
