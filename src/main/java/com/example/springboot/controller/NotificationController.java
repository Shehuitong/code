package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Notification;
import com.example.springboot.entity.User;
import com.example.springboot.service.NotificationService;
import com.example.springboot.service.UserService;
import com.example.springboot.util.JwtUtil; // 适配你的JwtUtil包路径
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 通知控制器：查询用户通知列表（适配现有JwtUtil和UserService）
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor // 自动注入依赖（包括JwtUtil）
public class NotificationController {

    // 注入所需服务和工具类
    private final NotificationService notificationService;
    private final UserService userService;
    private final JwtUtil jwtUtil; // 注入你的JwtUtil

    /**
     * 查询当前登录用户的通知列表
     * @param authorization 请求头中的token（格式：Bearer xxx）
     */
    @GetMapping("/notifications")
    public Result<List<Notification>> getUserNotifications(
            @RequestHeader(value = "Authorization", required = true) String authorization) {
        try {
            // 1. 校验Authorization头格式
            if (!authorization.startsWith("Bearer ")) {
                return Result.error("Authorization格式错误，需以'Bearer '开头（注意空格）");
            }
            // 提取纯token（去除"Bearer "前缀）
            String token = authorization.substring(7).trim();

            // 2. 解析token获取用户ID（使用你的JwtUtil中的getUserId方法）
            Long userId;
            try {
                userId = jwtUtil.getUserId(token); // 直接调用JwtUtil的getUserId方法
            } catch (Exception e) {
                // 捕获token解析异常（如无效、过期、签名错误等）
                return Result.error("token解析失败：" + e.getMessage());
            }

            // 3. 验证用户是否存在（通过UserService的getById方法，继承自IService）
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在（userId：" + userId + "）");
            }

            // 4. 查询用户的通知列表
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            return Result.success(notifications); // 使用你的Result.success方法
        } catch (Exception e) {
            // 捕获其他异常（如数据库查询失败）
            return Result.error("查询通知失败：" + e.getMessage());
        }
    }
}