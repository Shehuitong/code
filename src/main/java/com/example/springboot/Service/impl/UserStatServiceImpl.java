package com.example.springboot.service.impl;

import com.example.springboot.dto.UserStatDTO;
import com.example.springboot.entity.UserFavorites;
import com.example.springboot.mapper.UserFavoritesMapper;
import com.example.springboot.service.ActivityRegistrationService;
import com.example.springboot.service.UserFavoritesService;
import com.example.springboot.service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserStatServiceImpl implements UserStatService {

    private final ActivityRegistrationService registrationService;
    private final UserFavoritesMapper favoritesMapper;
    private final UserFavoritesService userFavoritesService;

    @Override
    public UserStatDTO getUserStats(Long userId) {
        UserStatDTO dto = new UserStatDTO();
        // 已报名活动数
        dto.setRegisteredActivityCount(registrationService.countByUserId(userId));
        // 收藏部门数
        dto.setCollectedDepartmentCount(userFavoritesService.countFavoriteDepartments(userId));
        // 收藏活动数
        dto.setCollectedActivityCount(userFavoritesService.countFavoriteActivities(userId));
        return dto;
    }
}