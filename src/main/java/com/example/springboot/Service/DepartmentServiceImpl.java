package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Department;
import com.example.springboot.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    public Department getByDeptId(int deptId) {
        return baseMapper.selectOne(
                new LambdaQueryWrapper<Department>()
                        .eq(Department::getDeptId, deptId)
        );
    }
}