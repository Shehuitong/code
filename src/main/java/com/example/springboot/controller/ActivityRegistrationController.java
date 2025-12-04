package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.ActivityRegistration;
import com.example.springboot.excption.BusinessErrorException; // 修正包名拼写：excption→exception
import com.example.springboot.service.ActivityRegistrationService;
import com.example.springboot.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registration") // 保持接口前缀一致
@RequiredArgsConstructor
public class ActivityRegistrationController {

    private final ActivityRegistrationService registrationService;
    private final JwtUtil jwtUtil;

    // 辅助方法：从Token获取当前登录用户ID（完善异常提示）
    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessErrorException("未授权：请在请求头携带有效的Bearer Token");
        }
        String token = authHeader.substring(7); // 截取"Bearer "后的Token部分
        try {
            return jwtUtil.getUserId(token); // 解析Token获取用户ID
        } catch (Exception e) {
            throw new BusinessErrorException("Token无效或已过期，请重新登录");
        }
    }

    /**
     * 用户报名活动
     * 请求示例：POST http://localhost:8080/api/registration/activity/1
     * 请求头：Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...（用户4的有效Token）
     */
    @PostMapping("/activity/{activityId}")
    public Result<ActivityRegistration> register(
            HttpServletRequest request,
            @PathVariable Long activityId
    ) {
        try {
            Long userId = getCurrentUserId(request); // 从Token解析用户ID（用户4的ID为4）
            ActivityRegistration registration = registrationService.registerActivity(userId, activityId);
            return Result.success(registration); // 传入报名记录作为数据
        } catch (BusinessErrorException e) {
            // 返回业务异常信息（如"年级不符合"、"已报名"等）
            return Result.error(e.getMessage());
        } catch (Exception e) {
            // 捕获其他未知异常
            return Result.error("报名失败：" + e.getMessage());
        }
    }

    /**
     * 用户取消报名
     * 请求示例：DELETE http://localhost:8080/api/registration/activity/1
     * 请求头：Authorization: Bearer <用户4的Token>
     */
    @DeleteMapping("/activity/{activityId}")
    public Result<ActivityRegistration> cancel(
            HttpServletRequest request,
            @PathVariable Long activityId
    ) {
        try {
            Long userId = getCurrentUserId(request);
            // 调用服务层方法，获取操作后的报名记录
            ActivityRegistration registration = registrationService.cancelRegistration(userId, activityId);
            return Result.success(registration); // 传入报名记录作为数据
        } catch (BusinessErrorException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("取消报名失败：" + e.getMessage());
        }
    }
}