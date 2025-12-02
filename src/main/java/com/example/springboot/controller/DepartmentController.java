
package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.DepartmentDetailDTO;
import com.example.springboot.dto.DepartmentUpdateDTO;
import com.example.springboot.entity.Admin;
import com.example.springboot.excption.BusinessErrorException;
import com.example.springboot.service.AdminService;
import com.example.springboot.service.DepartmentService;
import com.example.springboot.service.UserFavoritesService;
import com.example.springboot.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/department")
public class DepartmentController {
    @Autowired
    private UserFavoritesService favoritesService;
    private final DepartmentService departmentService;
    private final AdminService adminService;
    @Autowired
    private JwtUtil jwtUtil;
    public DepartmentController(DepartmentService departmentService,AdminService adminService) {
        this.departmentService = departmentService;
        this.adminService = adminService;
    }

    // 部门头像上传
    @PostMapping("/avatar")
    public Result<String> uploadDeptAvatar(
            @RequestParam("departmentId") Long departmentId,
            @RequestParam("file") MultipartFile file) {
        String avatarUrl = departmentService.uploadDeptAvatar(departmentId, file);
        return Result.success(avatarUrl);
    }

    //查看部门信息
    @GetMapping("/detail")
    public Result<DepartmentDetailDTO> getDepartmentDetail(@RequestParam("departmentId") Long departmentId) {
        DepartmentDetailDTO detailDTO = departmentService.getDepartmentDetail(departmentId);
        return Result.success(detailDTO);
    }

    // 编辑部门信息
    @PutMapping("/update")
    public Result<Boolean> updateDepartment(@RequestBody DepartmentUpdateDTO updateDTO) {
        boolean success = departmentService.updateDepartment(updateDTO);
        return Result.success(success);
    }

    @GetMapping("/follow-count")
    public Map<String, Object> getDepartmentFollowCount(HttpServletRequest request) {
        // 获取并解析token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证管理员身份
        String role = jwtUtil.getRoleFromToken(token);
        if (!"admin".equals(role)) {
            throw new BusinessErrorException("无权限访问，需管理员身份");
        }

        // 获取管理员信息
        Long adminId = jwtUtil.getUserId(token);
        Admin admin = adminService.getById(adminId);
        if (admin == null) {
            throw new BusinessErrorException("管理员不存在");
        }

        // 获取当前部门关注人数
        int followCount = favoritesService.countDepartmentFollowers(admin.getDepartmentId());

        Map<String, Object> result = new HashMap<>();
        result.put("departmentId", admin.getDepartmentId());
        result.put("followCount", followCount);
        result.put("message", "查询成功");
        return result;
    }
}