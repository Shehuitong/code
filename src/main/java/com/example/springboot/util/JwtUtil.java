package com.example.springboot.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.expiration:7200000}")
    private long expiration;  // 去掉static

    @Value("${jwt.secret:your-secret-key-32bytes-long-12345678}")
    private String secret;  // 去掉static

    /**
     * 生成Token（新增role参数区分角色）
     * @param userId 用户ID（管理员/用户的唯一标识）
     * @param username 用户名/管理员工号
     * @param role 角色标识（"admin"或"user"）
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, String role) {  // 去掉static
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)  // 新增角色声明
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    // 保持其他方法不变（getUserIdFromToken、parseToken），但均改为非static
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}