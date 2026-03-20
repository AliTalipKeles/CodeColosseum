package com.example.demo.dtos;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserInfoDto {
    private UUID id;
    private String username;
    private String email;
    private String password;
    private String role;
    private Instant account_create_date;
    private int rank;
    private int total_matches;

    public Map<String, Object> toMapWithoutPassword() {
        Map<String, Object> map = new HashMap<>();

        map.put("id", this.id);
        map.put("username", this.username);
        map.put("email", this.email);
        map.put("role", this.role);
        map.put("account_create_date", this.account_create_date);
        map.put("rank", this.rank);
        map.put("total_matches", this.total_matches);

    return map;
}
}
