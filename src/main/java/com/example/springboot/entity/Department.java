package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.springboot.enums.CollegeEnum;
import lombok.Data;

@Data
@TableName("Department")
public class Department {

    @TableId(value = "department_id", type = IdType.AUTO)
    private Long deptId;

    private String description;//部门描述

    @TableField("department_name")
    private String departmentName; // 部门名称

    @TableField("logo_url")
    private String avatar;//头像

    @TableField(value = "department_college", insertStrategy = FieldStrategy.NOT_EMPTY)
    private CollegeEnum departmentCollege;


}
