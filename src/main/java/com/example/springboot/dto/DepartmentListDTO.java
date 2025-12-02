package com.example.springboot.dto;

import com.example.springboot.entity.Activity;
import com.example.springboot.entity.Department;
import com.example.springboot.enums.CollegeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门搜索列表DTO：列表页显示的部门信息
 */
@Data
public class DepartmentListDTO {
    private Department department; // 部门基本信息
    private List<Activity> activities; // 部门所有活动（包括已下架）
}