package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.dto.DepartmentDetailDTO;
import com.example.springboot.dto.DepartmentUpdateDTO;
import com.example.springboot.entity.Department;
import org.springframework.web.multipart.MultipartFile;

public interface DepartmentService extends IService<Department> {
    // 根据部门ID查询部门信息
    Department getByDeptId(Long deptId);

    // 部门头像上传
    String uploadDeptAvatar(Long deptId, MultipartFile file);

    // 管理员头像上传
    String uploadAdminAvatar(Long adminId, MultipartFile file);

    // 查看部门详情（含该部门举办的活动）
    DepartmentDetailDTO getDepartmentDetail(Long deptId);

    // 编辑部门信息（名称和描述）
    boolean updateDepartment(DepartmentUpdateDTO updateDTO);
}