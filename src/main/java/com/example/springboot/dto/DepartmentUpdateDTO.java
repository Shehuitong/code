
package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepartmentUpdateDTO {
    @NotNull(message = "部门ID不能为空")
    private Long departmentId;

    @NotBlank(message = "部门名称不能为空")
    private String departmentName;

    private String description; // 部门描述（可为空）
}