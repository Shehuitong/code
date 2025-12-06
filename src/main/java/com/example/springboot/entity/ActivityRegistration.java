package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.springboot.enums.RegistrationStatusEnum;
import lombok.Data;

/**
 * 用户活动报名实体类（对应数据库UserActivityRegistration表）
 */
@Data
@TableName("UserActivityRegistration")
public class ActivityRegistration {
    @TableId(value = "registration_id", type = IdType.AUTO)
    private Long registrationId; // 报名记录主键ID

    @TableField(value = "user_id", insertStrategy = FieldStrategy.NOT_NULL)
    private Long userId; // 关联用户表ID（外键，非空）

    @TableField(value = "activity_id",insertStrategy = FieldStrategy.NOT_NULL)
    private Long activityId; // 关联活动表ID（外键，非空）

    @TableField(value = "registration_status", insertStrategy = FieldStrategy.NOT_NULL)
    private RegistrationStatusEnum registrationStatus; // 报名状态（非空，枚举）

    // 非数据库字段：关联查询用户信息
    @TableField(exist = false)
    private User user;

    // 非数据库字段：关联查询活动信息
    @TableField(exist = false)
    private Activity activity;
}