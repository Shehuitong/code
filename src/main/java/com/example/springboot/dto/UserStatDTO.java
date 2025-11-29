package com.example.springboot.dto;

import lombok.Data;

/**
 * 用户相关统计DTO（已报名活动数、收藏部门数、收藏活动数）
 */
@Data
public class UserStatDTO {
    private Integer registeredActivityCount; // 已报名活动数
    private Integer collectedDepartmentCount; // 收藏部门数（原关注部门数，统一命名为收藏）
    private Integer collectedActivityCount;  // 收藏活动数
}