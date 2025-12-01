// 新建 UserFavoritesController.java
package com.example.springboot.controller;

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
    public String add(@RequestParam Long targetId, @RequestParam String targetType) {
        favoritesService.addFavorite(targetId, targetType);
        return "收藏成功";
    }

    // 取消收藏
    @PostMapping("/cancel")
    public String cancel(@RequestParam Long targetId, @RequestParam String targetType) {
        favoritesService.cancelFavorite(targetId, targetType);
        return "取消收藏成功";
    }
}