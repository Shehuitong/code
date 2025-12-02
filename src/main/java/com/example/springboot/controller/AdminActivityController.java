package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.ActivityPublishDTO;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.Admin;
import com.example.springboot.service.ActivityService;
import com.example.springboot.service.AdminService;
import com.example.springboot.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityService activityService;
    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    /**
     * 管理员发布新活动
     */
    @PostMapping
    public Result<Activity> publishActivity(
            HttpServletRequest request,
            // 修复1：去掉多余的Activity，参数类型是ActivityPublishDTO；修复2：@RequestBodyBody拼写错误改为@RequestBody
            @Valid @RequestBody ActivityPublishDTO activityPublishDTO) {

        // 从Token中获取管理员ID
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("未授权：请传递有效的Token");
        }
        String token = authHeader.substring(7);
        Long adminId = jwtUtil.getUserId(token);

        // 获取管理员所属部门ID
        Admin admin = adminService.getById(adminId);
        if (admin == null) {
            return Result.error("管理员不存在");
        }

        // 发布活动（此时activityPublishDTO类型正确，可正常调用）
        Activity activity = activityService.publishActivity(activityPublishDTO, admin.getDepartmentId());
        return Result.success(activity);
    }
}