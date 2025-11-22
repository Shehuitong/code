package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("Department")
public class Department {

    @TableId(value = "department_id", type = IdType.AUTO)
    private int deptId;

    private String description;//部门描述

    private String department_name; // 部门名称

    @TableField("logo_url")
    private String avatar;//头像

}
