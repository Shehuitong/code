package com.example.springboot.service.impl;

import com.example.springboot.dto.UserActionResultDTO;
import com.example.springboot.entity.ActivityRegistration;
import com.example.springboot.entity.UserFavorites;
import com.example.springboot.service.ActivityRegistrationService;
import com.example.springboot.service.UserActionService;
import com.example.springboot.service.UserFavoritesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {

    private final ActivityRegistrationService registrationService;
    private final UserFavoritesService favoritesService;

    @Override
    public UserActionResultDTO getUserActionInfo(Long userId) {
        UserActionResultDTO resultDTO = new UserActionResultDTO();

        // 获取用户报名信息
        List<ActivityRegistration> registrations = registrationService.getRegistrationsByUserId(userId);
        resultDTO.setRegistrationList(registrations);

        // 获取用户收藏信息
        List<UserFavorites> favorites = favoritesService.getUserAllFavorites(userId);
        resultDTO.setFavoriteList(favorites);

        return resultDTO;
    }
}