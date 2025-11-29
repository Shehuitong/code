package com.example.springboot.dto;

import com.example.springboot.enums.CollegeEnum;
import com.example.springboot.enums.GradeEnum;
import lombok.Data;

@Data
public class UserProfileDTO {
    private String avatarUrl;    // 头像
    private String username;  // 姓名
    private String studentId; // 学号
    private CollegeEnum college;   // 学院
    private GradeEnum grade;     // 年级
    private String phone;     // 手机号
}