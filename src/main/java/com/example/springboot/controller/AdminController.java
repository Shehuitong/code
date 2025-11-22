package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.AdminDetailDTO;
import com.example.springboot.entity.Admin;
import com.example.springboot.Service.AdminService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 查看管理员资料
    @GetMapping("/info")
    public Result<Admin> getAdminInfo(@RequestParam("adminId") Long adminId) {
        // 调用IService继承的getById方法，通过adminId查询管理员信息
        Admin admin = adminService.getById(adminId);
        if (admin == null) {
            return Result.error("管理员不存在");
        }
        admin.setPassword(null); // 隐藏密码
        return Result.success(admin);
    }

    // 编辑管理员资料（仅密码）
    @PutMapping("/info")
    public Result<Boolean> updateAdminInfo(
            @RequestParam("adminId") Long adminId,
            @RequestBody Admin updateAdmin) {
        boolean success = adminService.updateAdminInfo(adminId, updateAdmin);
        return Result.success(success);
    }

    // 管理员头像上传
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @RequestParam("adminId") Long adminId,
            @RequestParam("file") MultipartFile file) {
        String avatarUrl = adminService.uploadAvatar(adminId, file);
        return Result.success(avatarUrl);
    }

    // 2. 查看管理员详情（含部门+活动信息）
    @GetMapping("/info/detail") // 新路径：/api/admin/info/detail，避免冲突
    public Result<AdminDetailDTO> getAdminDetailWithDept(@RequestParam("adminId") Long adminId) {
        AdminDetailDTO detailDTO = adminService.getAdminDetail(adminId);
        return Result.success(detailDTO);
    }
}