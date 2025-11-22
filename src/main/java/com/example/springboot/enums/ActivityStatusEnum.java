package com.example.springboot.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 活动状态枚举（对应Activity表status字段）
 */
@Getter
public enum ActivityStatusEnum {
    NOT_STARTED("报名未开始"),
    IN_PROGRESS("报名进行中"),
    CLOSED("报名截止"),
    OFFLINE("下架");

    @EnumValue
    private final String desc;

    ActivityStatusEnum(String desc) {
        this.desc = desc;
    }
}