package com.example.springboot.dto;

import com.example.springboot.entity.ActivityRegistration;
import com.example.springboot.entity.UserFavorites;
import lombok.Data;
import java.util.List;

/**
 * 整合用户的报名信息和收藏信息
 */
@Data
public class UserActionResultDTO {
    // 报名信息列表（直接使用ActivityRegistration实体，保持原结构）
    private List<ActivityRegistration> registrationList;

    // 收藏信息列表（直接使用UserFavorites实体，包含活动和部门收藏）
    private List<UserFavorites> favoriteList;
}