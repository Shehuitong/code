package com.example.springboot.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 活动报名用户Excel导出DTO
 */
@Data
public class ActivityRegistrationExcelDTO {
    @ExcelProperty(value = "序号", index = 0)
    private Integer serialNumber;

    @ExcelProperty(value = "用户姓名", index = 1)
    private String userName;

    @ExcelProperty(value = "用户学号", index = 2)
    private String studentId;

    @ExcelProperty(value = "用户学院", index = 3)
    private String college;

    @ExcelProperty(value = "用户年级", index = 4)
    private String grade;

    @ExcelProperty(value = "用户手机号", index = 5)
    private String phone;
}