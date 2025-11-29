package com.example.springboot.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 报名状态枚举（对应UserActivityRegistration表registration_status字段）
 */
@Getter
public enum RegistrationStatusEnum {
    APPLIED(1, "已报名"),
    CANCELLED(2, "已取消");

    private final Integer code;
    private final String desc;

    RegistrationStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}