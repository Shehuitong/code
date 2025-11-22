package com.example.springboot.controller;

import com.example.springboot.Service.ActivityRegistrationService;
import com.example.springboot.dto.UpdatePasswordDTO;
import com.example.springboot.dto.UpdateUserInfoDTO;
import com.example.springboot.dto.UserProfileDTO;
import com.example.springboot.entity.ActivityRegistration;
import com.example.springboot.excption.BusinessErrorException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.Service.UserService;
import com.example.springboot.entity.User;
import org.springframework.web.bind.annotation.PutMapping;
import com.example.springboot.common.Result;
import com.example.springboot.dto.UserRegisterDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final ActivityRegistrationService registrationService;

    // 构造器注入
    @Autowired
    public UserController(UserService userService, ActivityRegistrationService registrationService) {
        this.userService = userService;
        this.registrationService = registrationService;
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


    // 查看个人资料接口：返回 UserProfileDTO
    @GetMapping("/profile")
    public Result<UserProfileDTO> getUserProfile(@RequestParam Long userId) {
        UserProfileDTO profileDTO = userService.getUserInfo(userId);
        return Result.success(profileDTO);
    }


    // 2. 编辑资料接口：支持单独改手机号、单独改头像、或两者都改
    @PutMapping("/info")
    public Result<Boolean> updateUserInfo(@Valid @RequestBody UpdateUserInfoDTO updateUserInfoDTO) {
        Long userId = updateUserInfoDTO.getUserId();
        String newPhone = updateUserInfoDTO.getPhone();
        String newAvatarUrl = updateUserInfoDTO.getAvatar();
        boolean success = userService.updateUserInfo(userId, newPhone, newAvatarUrl);
        return Result.success(success);
    }

    // 4.密码修改接口
    @PostMapping("/password")
    public Result<Boolean> updatePassword(@Valid @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        // 提取参数
        Long userId = updatePasswordDTO.getUserId();
        String oldPassword = updatePasswordDTO.getOldPassword();
        String newPassword = updatePasswordDTO.getNewPassword();
        // 调用服务层方法
        boolean success = userService.updatePassword(userId, oldPassword, newPassword);
        return Result.success(success);
    }

    // 头像上传接口（添加日志排查）
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file) {

        // 新增日志：打印参数接收情况（关键排查）
        log.info("收到头像上传请求：userId={}，file是否为空={}，file参数名={}",
                userId, file.isEmpty(), file.getName());

        // 1. 校验文件是否为空（提前拦截）
        if (file.isEmpty()) {
            log.error("文件为空：用户未选择文件或参数未传递");
            throw new BusinessErrorException("请选择要上传的头像文件");
        }

        // 2. 文件类型校验（保持不变）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.matches("image/(jpg|jpeg|png)")) {
            log.error("文件格式错误：contentType={}", contentType);
            throw new BusinessErrorException("仅支持jpg/png格式图片");
        }

        // 3. 校验文件大小（5MB限制）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            log.error("文件过大：size={}KB，最大允许{}KB", file.getSize()/1024, maxSize/1024);
            throw new BusinessErrorException("图片大小不能超过5MB");
        }

        // 4. 校验用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            log.error("用户不存在：userId={}", userId);
            throw new BusinessErrorException("用户不存在");
        }

        // 5. 调用服务层上传
        String avatarUrl = userService.uploadAvatar(userId, file);
        log.info("上传成功：userId={}，avatarUrl={}", userId, avatarUrl);
        return Result.success(avatarUrl);
    }



    //6. 查看用户自己报名过的活动
    @GetMapping("/my-registrations")
    public Result<List<ActivityRegistration>> getMyRegistrations(@RequestParam("userId") Long userId) {
        List<ActivityRegistration> registrations = registrationService.getRegistrationsByUserId(userId);
        return Result.success(registrations);
    }

}