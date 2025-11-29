package com.example.springboot.dto;

import com.example.springboot.enums.CollegeEnum;
import lombok.Data;

/**
 * 部门搜索联想DTO：返回部门名称和所属学院（供前端下拉选择）
 */
@Data
public class DepartmentSuggestDTO {

    private String departmentName; // 部门名称（核心联想字段）

}