
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
    public Result<Map<String, Object>> getDepartmentFollowCount(@RequestParam Long departmentId) {
        // 非空校验：部门ID不能为空
        if (departmentId == null) {
            return Result.error("部门ID不能为空，请手动输入");
        }

        // 直接通过传入的部门ID查询关注人数
        int followCount = favoritesService.countDepartmentFollowers(departmentId);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("followCount", followCount);

        return Result.success(result);
    }
}