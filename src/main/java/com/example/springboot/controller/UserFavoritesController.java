// 新建 UserFavoritesController.java
package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.UserFavorites;
import com.example.springboot.service.UserFavoritesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorites")
public class UserFavoritesController {

    @Autowired
    private UserFavoritesService favoritesService;

    // 收藏
    @PostMapping("/add")
    public Result<UserFavorites> add(@RequestParam Long targetId, @RequestParam String targetType){
        UserFavorites favorite = favoritesService.addFavorite(targetId, targetType);
        return Result.success(favorite);  // 用Result包装收藏信息
    }

    // 取消收藏
    @PostMapping("/cancel")
    public Result<UserFavorites> cancel(@RequestParam Long targetId, @RequestParam String targetType) {
        UserFavorites canceledFavorite = favoritesService.cancelFavorite(targetId, targetType);
        return Result.success(canceledFavorite);  // 用Result包装取消后的信息
    }
}