package com.example.springboot.enums;

import com.baomidou.mybatisplus.annotation.EnumValue; // MyBatis-Plus枚举注解
import lombok.Getter;

@Getter
public enum RegistrationStatusEnum {
    APPLIED(1, "APPLIED", "已报名"),    // 数据库ENUM的字符值是"APPLIED"
    CANCELLED(2, "CANCELLED", "已取消");// 数据库ENUM的字符值是"CANCELLED"

    private final Integer code;       // 业务逻辑用的数值code（保留你的设计）
    @EnumValue // 关键：指定该字段映射数据库ENUM的字符值
    private final String dbValue;     // 与数据库ENUM项完全一致的字符值
    private final String desc;

    RegistrationStatusEnum(Integer code, String dbValue, String desc) {
        this.code = code;
        this.dbValue = dbValue;
        this.desc = desc;
    }

    // 可选：根据数据库字符值反向获取枚举（用于查询结果映射）
    public static RegistrationStatusEnum getByDbValue(String dbValue) {
        for (RegistrationStatusEnum status : values()) {
            if (status.getDbValue().equals(dbValue)) {
                return status;
            }
        }
        return null;
    }
}