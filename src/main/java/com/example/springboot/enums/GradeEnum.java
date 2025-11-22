package com.example.springboot.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 年级枚举：约束可选年级，前端下拉框的选项来源
 */
@Getter
public enum GradeEnum {
    GRADE_2022("2022级"),
    GRADE_2023("2023级"),
    GRADE_2024("2024级"),
    GRADE_2025("2025级"); // 可按需添加

    @EnumValue // 数据库存储的字段值（中文描述）
    private final String desc;

    GradeEnum(String desc) {
        this.desc = desc;
    }
}