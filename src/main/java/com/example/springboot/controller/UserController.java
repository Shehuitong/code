package com.example.springboot.controller;

import com.example.springboot.dto.*;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.Department;
import com.example.springboot.excption.BusinessErrorException;
import com.example.springboot.service.ActivityRegistrationService;
import com.example.springboot.entity.ActivityRegistration;
import com.example.springboot.service.UserFavoritesService;
import com.example.springboot.service.UserStatService;
import com.example.springboot.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.service.UserService;
import com.example.springboot.entity.User;
import com.example.springboot.common.Result;
import org.springframework.validation.annotation.Validated;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final ActivityRegistrationService registrationService;
    private final JwtUtil jwtUtil;
    private final UserFavoritesService favoritesService;
    private final UserStatService userStatService;
    // 构造器注入
    @Autowired
    public UserController(UserService userService, ActivityRegistrationService registrationService, JwtUtil jwtUtil, UserFavoritesService favoritesService,UserStatService userStatService) {
        this.userService = userService;
        this.registrationService = registrationService;
        this.jwtUtil = jwtUtil;
        this.favoritesService = favoritesService;
        this.userStatService = userStatService;
    }

    @GetMapping("/get")
    public String getUser(@RequestParam String userId) {
        try {
            Long id = Long.parseLong(userId.trim());
            // 实际业务逻辑：根据userId查询用户信息
            User user = userService.getById(id); // 使用IService提供的getById方法
            return "查询到用户ID：" + id + "，用户名：" + (user != null ? user.getUsername() : "用户不存在");
        } catch (NumberFormatException e) {
            return "userId格式错误，必须为纯数字";
        }
    }

    // 1.注册接口
    @PostMapping("/register")
    public Result<User> register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        User user = userService.register(registerDTO);
        // 脱敏处理（隐藏密码）
        user.setPassword(null);
        return Result.success(user);
    }


    // 2.查看个人资料接口：返回 UserProfileDTO
    @GetMapping("/info")
    public Result<UserProfileDTO> getMyInfo(HttpServletRequest request) {
        // 1. 提取并校验 Token（和报名活动接口逻辑一致，可复用）
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("未授权：请传递有效的 Token");
        }
        String token = authHeader.substring(7);

        // 2. 从 Token 提取当前登录用户ID（核心，避免越权）
        Long currentUserId;
        try {
            currentUserId = jwtUtil.getUserId(token);
        } catch (Exception e) {
            return Result.error("Token 无效或已过期");
        }

        // 3. 查询当前用户的个人信息（业务核心）
        UserProfileDTO userInfo = userService.getUserInfo(currentUserId);
        return Result.success(userInfo);
    }


    // 3. 编辑资料接口：支持单独改手机号、单独改头像、或两者都改
    @PutMapping("/update-info")
    public Result<Boolean> updateUserInfo(
            HttpServletRequest request,
            @Valid @RequestBody UpdateUserInfoDTO updateUserInfoDTO
    ) {
        // 1. 提取并校验Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("未授权：请传递有效的Token");
        }
        String token = authHeader.substring(7);

        // 2. 从Token提取当前登录用户的userId（核心：避免前端传参，防止越权）
        Long currentUserId;
        try {
            currentUserId = jwtUtil.getUserId(token);
        } catch (Exception e) {
            return Result.error("Token无效或已过期，请重新登录");
        }

        // 3. 调用Service修改资料（传入Token提取的userId，确保只能修改自己的）
        boolean success = userService.updateUserInfo(
                currentUserId,  // 从Token来，无法篡改
                updateUserInfoDTO.getPhone()
        );

        return Result.success(success);
    }

    // 4.密码修改接口
    @PutMapping("/password")
    public Result<Boolean> updatePassword(
            HttpServletRequest request,
            @Valid @RequestBody UpdatePasswordDTO dto
    ) {
        // 1. 提取并校验Token格式（和/info接口保持一致）
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            log.error("修改密码失败：请求头缺少Authorization");
            return Result.error("未授权：请传递有效Token");
        }
        if (!authHeader.startsWith("Bearer ")) {
            log.error("修改密码失败：Token格式错误，authHeader={}", authHeader);
            return Result.error("Token格式错误：需以Bearer开头（格式：Authorization: Bearer <Token>）");
        }

        // 2. 提取Token并解析userId（完整异常捕获+兜底校验）
        String token = authHeader.substring(7);
        Long currentUserId;
        try {
            currentUserId = jwtUtil.getUserId(token);
            // 兜底：确保解析出的userId非空
            if (currentUserId == null) {
                log.error("修改密码失败：Token中未获取到userId，token={}", token);
                return Result.error("用户身份无效，请重新登录");
            }
            log.info("修改密码请求：currentUserId={}", currentUserId);
        } catch (ExpiredJwtException e) {
            log.error("修改密码失败：Token已过期，token={}", token);
            return Result.error("Token已过期，请重新登录");
        } catch (Exception e) {
            log.error("修改密码失败：Token解析异常，token={}，异常={}", token, e.getMessage());
            return Result.error("Token无效，请传递有效Token");
        }

        // 3. 调用Service修改密码（此时currentUserId一定非空）
        boolean success = userService.updatePassword(
                currentUserId,
                dto.getOldPassword(),
                dto.getNewPassword()
        );

        // 4. 处理业务结果（返回明确提示）
        if (!success) {
            log.error("修改密码失败：原密码错误或系统异常，currentUserId={}", currentUserId);
            return Result.error("修改密码失败：原密码错误，请重试");
        }
        log.info("修改密码成功：currentUserId={}", currentUserId);
        return Result.success(true);
    }

    // 5.头像上传接口
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @RequestHeader("Authorization") String authHeader,  // 接收完整的Authorization头
            @RequestParam("file") MultipartFile file
    ) {
        // 1. 校验并提取Token（与其他接口保持一致的逻辑）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("未授权：请传递有效的Token（格式：Authorization: Bearer <Token>）");
        }
        String token = authHeader.substring(7);  // 去掉"Bearer "前缀（7个字符）

        // 2. 解析用户ID
        Long userId;
        try {
            userId = jwtUtil.getUserId(token);
        } catch (Exception e) {
            return Result.error("Token无效或已过期，请重新登录");
        }

        // 3. 上传头像
        String avatarUrl = userService.uploadAvatar(userId, file);
        return Result.success(avatarUrl);
    }


    //6. 查看用户自己报名过的活动
    @GetMapping("/my-registrations")
    public Result<List<ActivityRegistrationDTO>> getMyRegistrations(HttpServletRequest request) {
        try {
            // 1. 获取当前登录用户ID（复用工具方法）
            Long currentUserId = getCurrentUserId(request);
            log.info("用户[{}]查询已报名活动", currentUserId);

            // 2. 调用Service获取DTO列表
            List<ActivityRegistrationDTO> registrationDTOs = registrationService.getUserRegistrationDTOs(currentUserId);

            // 3. 返回结果
            return Result.success(registrationDTOs);
        } catch (BusinessErrorException e) {
            log.error("查询已报名活动失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询已报名活动发生系统异常", e);
            return Result.error("查询失败，请稍后重试");
        }
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessErrorException("未授权：请传递有效的Token"); // 或返回错误Result
        }
        String token = authHeader.substring(7);
        try {
            return jwtUtil.getUserId(token);
        } catch (Exception e) {
            throw new BusinessErrorException("Token无效或已过期，请重新登录"); // 或返回错误Result
        }
    }


    //7.查看用户收藏的部门
    @GetMapping("/favorites/departments")
    public Result<List<Department>> getMyFavoriteDepartments(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request); // 复用逻辑
        List<Department> departments = favoritesService.getFavoriteDepartmentsInfo(currentUserId);
        return Result.success(departments);
    }


    //8.查看用户关注的活动
    @GetMapping("/favorites/activities")
    public Result<List<ActivityDetailDTO>> getMyFavoriteActivities(HttpServletRequest request) { // 修改返回类型
        Long currentUserId = getCurrentUserId(request);
        List<ActivityDetailDTO> activities = favoritesService.getFavoriteActivitiesInfo(currentUserId); // 接收DTO列表
        return Result.success(activities);

    }

    // 新增：获取用户统计信息
    @GetMapping("/stats")
    public Result<UserStatDTO> getUserStats(HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            // 委托给 UserStatService 处理业务逻辑，控制器只负责请求响应
            UserStatDTO statsDTO = userStatService.getUserStats(currentUserId);
            return Result.success(statsDTO);
        } catch (BusinessErrorException e) {
            log.error("获取用户统计信息失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取用户统计信息发生系统异常", e);
            return Result.error("获取统计信息失败，请稍后重试");
        }
    }

}
