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
    // 新增：收藏活动接口（固定targetType为ACTIVITY）
    @PostMapping("/activity/add")
    public Result<UserFavorites> addActivityFavorite(@RequestParam Long activityId) {
        UserFavorites favorite = favoritesService.addFavorite(activityId, UserFavorites.TYPE_ACTIVITY);
        return Result.success(favorite);
    }

    // 新增：取消收藏活动接口
    @PostMapping("/activity/cancel")
    public Result<UserFavorites> cancelActivityFavorite(@RequestParam Long activityId) {
        UserFavorites canceledFavorite = favoritesService.cancelFavorite(activityId, UserFavorites.TYPE_ACTIVITY);
        return Result.success(canceledFavorite);
    }

    // 新增：收藏部门接口（固定targetType为DEPARTMENT）
    @PostMapping("/department/add")
    public Result<UserFavorites> addDepartmentFavorite(@RequestParam Long departmentId) {
        UserFavorites favorite = favoritesService.addFavorite(departmentId, UserFavorites.TYPE_DEPARTMENT);
        return Result.success(favorite);
    }

    // 新增：取消收藏部门接口
    @PostMapping("/department/cancel")
    public Result<UserFavorites> cancelDepartmentFavorite(@RequestParam Long departmentId) {
        UserFavorites canceledFavorite = favoritesService.cancelFavorite(departmentId, UserFavorites.TYPE_DEPARTMENT);
        return Result.success(canceledFavorite);
    }
}