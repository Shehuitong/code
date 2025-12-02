package com.example.springboot.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum RegistrationStatusEnum {
    // 调整dbValue为数据库实际存储的中文
    APPLIED(1, "已报名", "已报名"),    // 数据库ENUM的字符值是"已报名"
    CANCELLED(2, "已取消", "已取消");// 数据库ENUM的字符值是"已取消"

    private final Integer code;       // 业务逻辑用的数值code
    @EnumValue // 该字段映射数据库ENUM的中文值
    private final String dbValue;     // 与数据库ENUM项完全一致的中文值
    private final String desc;        // 描述（这里和dbValue一致，也可根据需要单独定义）

    RegistrationStatusEnum(Integer code, String dbValue, String desc) {
        this.code = code;
        this.dbValue = dbValue;
        this.desc = desc;
    }


    // 根据数据库存储的中文值反向获取枚举
    public static RegistrationStatusEnum getByDbValue(String dbValue) {
        for (RegistrationStatusEnum status : values()) {
            if (status.getDbValue().equals(dbValue)) {
                return status;
            }
        }
        return null;
    }
}