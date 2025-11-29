package com.example.springboot.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName("DepartmentAdmin") // 绑定数据库管理员表
public class Admin {
    @TableId(value = "admin_id", type = IdType.AUTO)
    private Long Id;

    private Long department_id;
    @TableField(exist = false) // 标识该字段不在数据库表中
    private Department department;

    @TableField("employee_id")
    private String workId;//管理员工号

    @TableField("admin_password")
    private String password; // 密码

    @TableField("admin_name")
    private String AdminName; // 昵称

    @TableField("admin_avatar_url")
    private String avatar;//头像
}