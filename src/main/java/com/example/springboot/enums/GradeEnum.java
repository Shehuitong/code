
package com.example.springboot.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonValue; // 新增导入

    /**
     * 年级组合枚举（仅中文描述，与原 GradeEnum 风格完全一致）
     */
    @Getter
    public enum GradeEnum {
        // 单个年级（与原枚举中文表述一致）
        GRADE_2022("2022级"),
        GRADE_2023("2023级"),
        GRADE_2024("2024级"),
        GRADE_2025("2025级"),

        // 两个年级组合（中文加号拼接）
        GRADE_2022_2023("2022级+2023级"),
        GRADE_2022_2024("2022级+2024级"),
        GRADE_2022_2025("2022级+2025级"),
        GRADE_2023_2024("2023级+2024级"),
        GRADE_2023_2025("2023级+2025级"),
        GRADE_2024_2025("2024级+2025级"),

        // 三个年级组合（中文加号拼接）
        GRADE_2022_2023_2024("2022级+2023级+2024级"),
        GRADE_2022_2023_2025("2022级+2023级+2025级"),
        GRADE_2022_2024_2025("2022级+2024级+2025级"),
        GRADE_2023_2024_2025("2023级+2024级+2025级"),

        // 全部年级组合（纯中文描述）
        GRADE_ALL("2022级+2023级+2024级+2025级");

        // 与原 GradeEnum 完全一致：仅中文描述字段，用于数据库存储和前端展示
        @EnumValue
        @JsonValue
        private final String desc;

        // 与原 GradeEnum 一致的构造方法（仅接收中文描述）
        GradeEnum(String desc) {
            this.desc = desc;
        }
    }