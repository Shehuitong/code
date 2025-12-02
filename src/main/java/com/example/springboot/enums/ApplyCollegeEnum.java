package com.example.springboot.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue; // 新增导入
import lombok.Getter;

/**
 * 活动报名对象学院枚举（对应Activity表apply_college字段）
 */
@Getter
public enum ApplyCollegeEnum {
    ALL("全校"),
    COMPUTER("计算机学院"),
    LAW("法学院"),
    CHEMISTRY("化学学院"),
    FOREIGN_LANGUAGE("外国语学院"),
    ELECTRICAL("电气学院"),
    MECHANICAL("机械学院"),
    CIVIL("土木学院"),
    MEDICAL("医学院"),
    MATH_STAT("数统学院"),
    ECONOMIC_MANAGEMENT("经管学院");

    @EnumValue
    @JsonValue // 新增注解，指定JSON反序列化依据desc字段
    private final String desc;

    ApplyCollegeEnum(String desc) {
        this.desc = desc;
    }
}