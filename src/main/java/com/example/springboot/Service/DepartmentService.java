package com.example.springboot.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Department;

public interface DepartmentService extends IService<Department> {
    // 根据部门ID查询部门信息
    Department getByDeptId(int deptId);
}