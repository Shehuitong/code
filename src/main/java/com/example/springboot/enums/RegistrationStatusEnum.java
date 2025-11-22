package com.example.springboot.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 报名状态枚举（对应UserActivityRegistration表registration_status字段）
 */
@Getter
public enum RegistrationStatusEnum {
    REGISTERED("已报名"),
    CANCELED("已取消");

    @EnumValue
    private final String desc;

    RegistrationStatusEnum(String desc) {
        this.desc = desc;
    }
}