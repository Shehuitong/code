package com.example.springboot.dto;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserInfoDTO {

    // 手机号校验：和注册时完全一致（11位有效手机号），可选传递（不传递则不更新）
    @Pattern(
            regexp = "^1[3-9]\\d{9}$",
            message = "手机号格式不正确，需输入11位有效手机号（如13800138000）"
    )
    private String phone;

}
