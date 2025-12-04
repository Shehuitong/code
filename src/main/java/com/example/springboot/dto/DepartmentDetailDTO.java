// DepartmentDetailDTO.java
package com.example.springboot.dto;

import com.example.springboot.entity.Activity;
import com.example.springboot.entity.Department;
import lombok.Data;
import java.util.List;

@Data
public class DepartmentDetailDTO {
    private Department department; // 部门基本信息
    private List<Activity> activities; // 部门举办的活动
    // 新增：部门关注人数
    private Integer followCount;
}