package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.springboot.enums.CollegeEnum;
import lombok.Data;

@Data
@TableName("Department")
public class Department {

    @TableId(type = IdType.AUTO, value = "department_id") // 主键注解
    @TableField(value = "department_id") // 显式指定数据库字段名（和数据库完全一致，包括大小写）
    private Long deptId;

    private String description;//部门描述

    @TableField("department_name")
    private String departmentName; // 部门名称

    @TableField("logo_url")
    private String avatar;//头像

    @TableField(value = "department_college", insertStrategy = FieldStrategy.NOT_EMPTY)
    private CollegeEnum departmentCollege;


}
