package com.example.springboot.dto;

import com.example.springboot.entity.Activity;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.Department;
import lombok.Data;

import java.util.List;

@Data
public class AdminDetailDTO {
    private Admin admin; // 管理员个人信息
    private Department department; // 关联的部门信息
    private List<Activity> activities; // 部门发布的活动列表
}