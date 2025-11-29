package com.example.springboot.dto;

import com.example.springboot.entity.Department;
import com.example.springboot.enums.CollegeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门搜索列表DTO：列表页显示的部门信息
 */
@Data
public class DepartmentListDTO {
    private Long departmentId; // 部门ID
    private String departmentName; // 部门名称
    private String description; // 部门描述
    private String logoUrl; // 部门头像
    private CollegeEnum departmentCollege; // 所属学院
    private LocalDateTime createTime; // 创建时间
}