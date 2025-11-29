package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    // 表名改为大写 Department，与实体类 @TableName("Department") 匹配
    @Select("SELECT department_id, department_name, description, logo_url FROM Department WHERE department_id = #{id}")
    Department selectDeptWithName(Long id);
}