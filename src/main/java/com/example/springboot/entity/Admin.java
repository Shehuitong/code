package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("DepartmentAdmin") // 必须和数据库管理员表名完全一致（H2区分大小写）
public class Admin {
    @TableId(value = "admin_id", type = IdType.AUTO)
    private Long Id; // 主键（对应数据库 admin_id）

    // 部门ID
    @TableField(value = "department_id")
    private Long department_id; // 保持和你代码一致（后续可统一驼峰，当前先兼容）

    // 工号（对应数据库 employee_id）
    @TableField(value = "employee_id") // 显式绑定数据库字段
    private String workId;

    // 头像
    @TableField(value = "admin_avatar_url")
    private String avatar;

    // 管理员名称
    @TableField(value = "admin_name")
    private String adminName;

    // 密码
    @TableField(value = "admin_password")
    private String password;

    // 非数据库字段（关联部门，无需映射）
    @TableField(exist = false)
    private Department department;
}