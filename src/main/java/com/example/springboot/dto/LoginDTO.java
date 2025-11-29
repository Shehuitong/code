package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 登录请求参数DTO
 */
@Data
public class LoginDTO {
    /** 账号（学生填学号=studentId，管理员填工号=workId） */
    @NotBlank(message = "账号（学/工号）不能为空")
    @Pattern(regexp = "^(20(22|23|24|25)\\d{5}|G2025\\d{3})$",
            message = "账号格式错误！用户账号：2022-2025+3位学院数+2位座位号；管理员账号：G2025+3位数字")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,12}$",
            message = "密码必须为8~12位，且包含数字、大写字母和小写字母"
    )
    private String password;
}