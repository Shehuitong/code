package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.AdminPersonalInfoDTO;
import com.example.springboot.dto.AdminUpdatePasswordDTO;
import com.example.springboot.excption.BusinessErrorException;
import com.example.springboot.service.AdminService;
import com.example.springboot.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    // 构造器注入（无字段注入警告）
    @Autowired
    public AdminController(AdminService adminService,JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;

    }

    // 1. 查看管理员个人信息
    @GetMapping("/my-info")
    public Result<AdminPersonalInfoDTO> getAdminMyInfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("未授权：请传递有效的 Token");
        }
        String token = authHeader.substring(7);
        Long currentAdminId = jwtUtil.getUserId(token); // 假设管理员ID也存在 Token 中

        AdminPersonalInfoDTO adminInfo = adminService.getAdminPersonalInfo(currentAdminId);
        return Result.success(adminInfo);
    }

    // 2. 独立修改密码接口（对应“修改密码”按钮）
    @PutMapping("/update-password")
    public Result<Boolean> updatePassword(
            HttpServletRequest request,
            @Valid @RequestBody AdminUpdatePasswordDTO dto  // 仅接收密码相关参数
    ) {
        // 1. 提取并校验 Token（核心：自动识别管理员身份）
        String authHeader = request.getHeader("Authorization");
        log.info("管理员修改密码请求：Authorization头={}，旧密码是否传递={}",
                authHeader, dto.getOldPassword() != null);

        // Token 非空+格式校验
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("管理员修改密码失败：未传递有效Bearer Token");
            throw new BusinessErrorException("未授权：请传递管理员有效Token（格式：Authorization: Bearer <Token>）");
        }

        // 解析 Token 获取管理员ID（Token有效即代表管理员存在，无需额外校验）
        String token = authHeader.substring(7);
        Long currentAdminId;
        try {
            // 若Token中存储的是adminId字段，替换为 jwtUtil.getAdminId(token)
            currentAdminId = jwtUtil.getUserId(token);
            log.info("管理员Token解析成功：currentAdminId={}", currentAdminId);
        } catch (Exception e) {
            log.error("管理员Token无效：token={}，异常={}", token, e.getMessage());
            throw new BusinessErrorException("Token无效或已过期，请重新登录");
        }

        // 2. 调用 Service 修改密码（传入Token提取的adminId，确保只能修改自己的密码）
        boolean success = adminService.updateAdminPassword(
                currentAdminId,  // 从Token提取，无法篡改
                dto.getOldPassword(),
                dto.getNewPassword()
        );

        if (success) {
            log.info("管理员密码修改成功：currentAdminId={}", currentAdminId);
        } else {
            log.error("管理员密码修改失败：原密码错误或数据异常，currentAdminId={}", currentAdminId);
            // 可根据Service逻辑调整提示（若Service已抛异常，此处无需额外处理）
            throw new BusinessErrorException("原密码错误或修改失败，请重试");
        }

        return Result.success(success);
    }

    // 3. 独立头像上传接口（对应“编辑资料”按钮，核心需求）
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file // 仅需传递头像文件
    ) {
        // 1. 提取并校验Token（核心：自动识别管理员身份）
        String authHeader = request.getHeader("Authorization");
        log.info("管理员头像上传请求：Authorization头={}，file是否为空={}", authHeader, file.isEmpty());

        // Token格式校验
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("管理员头像上传失败：未传递有效Bearer Token");
            throw new BusinessErrorException("未授权：请传递管理员有效Token（格式：Authorization: Bearer <Token>）");
        }

        // 解析Token获取管理员ID（无需校验存在性：Token有效即代表管理员存在）
        String token = authHeader.substring(7);
        Long currentAdminId;
        try {
            currentAdminId = jwtUtil.getUserId(token); // 若Token存储字段是adminId，替换为getAdminId()
            log.info("管理员Token解析成功：currentAdminId={}", currentAdminId);
        } catch (Exception e) {
            log.error("管理员Token无效：token={}，异常={}", token, e.getMessage());
            throw new BusinessErrorException("Token无效或已过期，请重新登录");
        }

        // 2. 核心文件校验（避免无效请求，保留必要逻辑）
        if (file.isEmpty()) {
            log.error("管理员头像上传失败：文件为空，adminId={}", currentAdminId);
            throw new BusinessErrorException("请选择要上传的头像文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.matches("image/(jpg|jpeg|png)")) {
            log.error("管理员头像上传失败：格式错误（{}），adminId={}", contentType, currentAdminId);
            throw new BusinessErrorException("仅支持jpg/png格式图片");
        }
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            log.error("管理员头像上传失败：文件过大（{}KB），adminId={}", file.getSize()/1024, currentAdminId);
            throw new BusinessErrorException("图片大小不能超过5MB");
        }

        // 3. 直接调用服务层上传（Token有效即管理员存在，无需重复校验）
        String avatarUrl = adminService.uploadAvatar(currentAdminId, file);
        log.info("管理员头像上传成功：adminId={}，头像URL={}", currentAdminId, avatarUrl);

        return Result.success(avatarUrl);
    }
}