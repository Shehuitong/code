package com.example.springboot.dto;

import com.example.springboot.enums.CollegeEnum;
import com.example.springboot.enums.GradeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求参数DTO
 */
@Data
public class UserRegisterDTO {

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度必须在2-20位之间")
    private String userName; // 姓名

    @Pattern(
            regexp = "^202[2-5]\\d{3}\\d{2}$",
            message = "学号格式错误，应为：4位年级（2022-2025）+3位学院号+2位座位号（共9位）"
    )
    private String studentId;


    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,16}$", message = "密码需包含大小写字母和数字，长度8-12位")
    private String password; // 密码

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword; // 确认密码

    @NotNull(message = "学院不能为空")
    private CollegeEnum college; // 学院（枚举类型）

    @NotNull(message = "年级不能为空")
    private GradeEnum grade; // 年级（枚举类型）

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone; // 手机号
}