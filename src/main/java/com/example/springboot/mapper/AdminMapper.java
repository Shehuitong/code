package com.example.springboot.mapper;

import com.example.springboot.entity.Admin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AdminMapper extends BaseMapper<Admin> {

    // 原有方法（保留）
    Admin getByWorkId(@Param("workId") String workId);

    // 新增：自定义查询管理员，显式包含 department_id 字段
    @Select("""
        SELECT 
            admin_id, 
            employee_id, 
            admin_password, 
            admin_name, 
            admin_avatar_url, 
            department_id  -- 强制查询该字段，避免漏查
        FROM DepartmentAdmin 
        WHERE admin_id = #{adminId}
    """)
    Admin selectAdminByIdWithDept(@Param("adminId") Long adminId);
}