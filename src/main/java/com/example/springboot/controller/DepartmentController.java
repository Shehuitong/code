
package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.DepartmentDetailDTO;
import com.example.springboot.dto.DepartmentUpdateDTO;
import com.example.springboot.service.DepartmentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
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
}