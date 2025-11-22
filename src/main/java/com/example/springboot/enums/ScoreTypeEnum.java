package com.example.springboot.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 活动加分类型枚举（对应Activity表score_type字段）
 */
@Getter
public enum ScoreTypeEnum {
    TYPE_11("1.1.科技竞赛"),
    TYPE_12("1.2.文体竞赛及其他比赛"),
    TYPE_13("1.3.创新实践"),
    TYPE_14("1.4.人文与科学素养"),
    TYPE_15("1.5.社会工作"),
    TYPE_16("1.6.学生活动"),
    TYPE_17("1.7.荣誉加分"),
    TYPE_18("1.8.提升劳动素养");

    @EnumValue
    private final String desc;

    ScoreTypeEnum(String desc) {
        this.desc = desc;
    }
}
