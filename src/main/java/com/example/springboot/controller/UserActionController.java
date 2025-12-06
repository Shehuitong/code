package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.UserActionResultDTO;
import com.example.springboot.entity.ActivityRegistration;
import com.example.springboot.entity.UserFavorites;
import com.example.springboot.service.ActivityRegistrationService;
import com.example.springboot.service.UserFavoritesService;
import com.example.springboot.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserActionController {

    @Autowired
    private ActivityRegistrationService registrationService;

    @Autowired
    private UserFavoritesService favoritesService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/actions")
    public Result<UserActionResultDTO> getUserActions(HttpServletRequest request) {
        // 解析用户ID
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);

        // 1. 查询报名记录（仅核心字段，不含活动/用户详情）
        List<ActivityRegistration> registrationList = registrationService.getRegistrationsByUserId(userId);
        // 清除可能存在的关联对象（双重保障）
        registrationList.forEach(reg -> {
            reg.setUser(null);
            reg.setActivity(null);
        });

        // 2. 查询收藏记录（保持原逻辑，如需简化可同理处理）
        List<UserFavorites> favoriteList = favoritesService.lambdaQuery()
                .eq(UserFavorites::getUserId, userId)
                .list();

        // 3. 封装返回结果
        UserActionResultDTO resultDTO = new UserActionResultDTO();
        resultDTO.setRegistrationList(registrationList);
        resultDTO.setFavoriteList(favoriteList);

        return Result.success(resultDTO);
    }
}