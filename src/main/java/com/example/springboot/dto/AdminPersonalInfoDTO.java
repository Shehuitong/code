// AdminPersonalInfoDTO.java
package com.example.springboot.dto;

import lombok.Data;

@Data
public class AdminPersonalInfoDTO {
    private String adminName;    // 管理员昵称
    private String workId;       // 工号
    private String avatar;       // 头像URL
    private String departmentName; // 所属部门名称
    private Long deptId;
}