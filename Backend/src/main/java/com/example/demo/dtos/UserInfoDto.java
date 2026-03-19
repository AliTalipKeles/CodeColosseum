package com.example.demo.dtos;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserInfoDto {
    private UUID id;
    private String username;
    private String email;
    private String password;
    private String role;
    private Instant account_create_date;
    private int rank;
    private int total_matches;

    public UserInfoDto() {
    }
    public void setUUID(UUID id){
        this.id = id;
    }
    public void setEmail(String email){
        this.email=email;
    } 
    public void setUsername(String username){
        this.username= username;
    } 
    public void setPassword(String password){
        this.password= password;
    } 
    public void setRole(String role){
        this.role= role;
    }
    public void setAccountCreateDate(Instant date){
        this.account_create_date= date;
    } 
    public void setRank(int rank){
        this.rank= rank;
    }  
    public void setTotalMatches(int total_matches){
        this.total_matches=total_matches;
    }
    public String getUsername(){
        return this.username;
    }
    public UUID getUUID(){
        return this.id;
    }
    public String getEmail(){
        return this.email;
    }
    public String getPassword(){
        return this.password;
    }
    public String getRole(){
        return this.role;
    }
    public int getTotalMatches(){
        return this.total_matches;
    }
    public Instant getAccountCreateDate(){
        return this.account_create_date;
    }
    public Integer getRank(){
        return this.rank;
    }

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
