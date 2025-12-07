package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知表：存储用户收到的提醒
 */
@Data
@TableName("Notification")  // 与数据库表名对应
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long notificationId;  // 对应数据库notification_id（自增主键）

    private Long userId;  // 对应数据库user_id（关联用户表）

    @TableField("notify_type")
    private String notifyType;  // 通知类型（对应数据库enum类型，Java用String接收）

    @TableField("related_activity_id")
    private Long relatedActivityId;  // 关联活动ID

    @TableField("related_department_id")
    private Long relatedDepartmentId;//部门ID

    private String content;  // 通知内容（对应数据库enum类型，Java用String接收）

    @TableField("send_time")
    private LocalDateTime sendTime;

    public static final String TYPE_DEPARTMENT_NEW_ACTIVITY = "部门发布新活动通知";
    public static final String TYPE_ACTIVITY_EDITED = "活动编辑通知";
    public static final String TYPE_ACTIVITY_REGISTRATION_REMINDER = "活动报名开始提醒";
}