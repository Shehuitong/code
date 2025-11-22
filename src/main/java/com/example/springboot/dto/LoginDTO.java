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
    private String account;

    @NotBlank(message = "密码不能为空")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,12}$",
            message = "密码必须为8~12位，且包含数字、大写字母和小写字母"
    )
    private String password;
}