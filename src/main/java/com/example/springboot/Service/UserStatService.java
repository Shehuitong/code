package com.example.springboot.service;

import com.example.springboot.dto.UserStatDTO;

public interface UserStatService {
    /**
     * 获取用户统计数据
     * @param userId 用户ID
     * @return 包含报名数、关注数、收藏数的DTO
     */
    UserStatDTO getUserStats(Long userId);
}