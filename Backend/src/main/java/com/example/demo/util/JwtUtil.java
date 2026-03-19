package com.example.demo.util;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    private static final String SECRET = "my-super-secret-key-my-super-secret-key";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    
    public static String generateToken(UUID id,String username, String role) {
        return Jwts.builder()
                .setSubject(id.toString())
                .claim("username",username)
                .claim("role", role)
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .setIssuedAt(new Date())
                .setIssuer("codeColosseum-Auth")
                .signWith(KEY)
                .compact();
    }

    public static Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            return null;
        }
    }
}
