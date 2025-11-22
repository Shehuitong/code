package com.example.springboot.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // 全参构造器（参数顺序：token, role, id）
public class LoginResponse {
    private String token; // JWT令牌
    private String role; // 角色（admin/user）
    private Long id; // ID（管理员/用户，Long类型）
}