package com.example.springboot.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 活动承办学院枚举（对应Activity表hold_college字段）
 */
@Getter
public enum HoldCollegeEnum {
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
    private final String desc;

    HoldCollegeEnum(String desc) {
        this.desc = desc;
    }
}