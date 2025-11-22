package com.example.springboot.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 学院枚举（与数据库User表college字段枚举一致）
 */
@Getter
public enum CollegeEnum {
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

    @EnumValue // 数据库存储的中文值
    private final String desc;

    CollegeEnum(String desc) {
        this.desc = desc;
    }
}