package com.example.springboot.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
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
    public String generateToken(Long userId, String username, String role) {
        // 1. 密钥强制用UTF-8编码（避免系统默认编码生成非法字符）
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        // 2. 生成Token（仅含合法字段，无特殊字符）
        String token = Jwts.builder()
                .claim("userId", userId) // 仅存数字（无特殊字符）
                .claim("username", username.trim()) // 去除用户名前后隐藏空格
                .claim("role", role.trim()) // 去除角色前后隐藏空格
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256) // 明确算法，避免默认值差异
                .compact();

        // 3. 最终过滤：确保无隐藏字符（零宽空格、换行符等）
        return token.replaceAll("[^A-Za-z0-9_\\.-]", "");
    }


    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    public Long getAdminId(String token) {
        Claims claims = parseToken(token);
        return claims.get("adminId", Long.class); // 对应Token中管理员ID的实际字段名
    }

    // 在JwtUtil类中添加方法
    public Long getUserId(String token) {
        Claims claims = parseToken(token); // 需确保parseToken是JwtUtil中已实现的Token解析方法
        return claims.get("userId", Long.class); // 从Token的Claims中提取userId
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