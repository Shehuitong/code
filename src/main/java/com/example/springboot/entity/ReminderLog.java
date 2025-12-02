package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提醒日志实体类（记录用户接收活动提醒的日志，用于去重）
 */
@Data
@TableName("reminder_log") // 对应数据库表名
public class ReminderLog {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联user表）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 活动ID（关联activity表，新增字段）
     */
    @TableField("activity_id")
    private Long activityId;

    /**
     * 提醒发送时间
     */
    @TableField("send_time")
    private LocalDateTime sendTime;

    /**
     * 备注（可选，记录额外信息）
     */
    @TableField("remark")
    private String remark;

    // 无参构造
    public ReminderLog() {
    }

    // 带参构造（常用字段）
    public ReminderLog(Long userId, Long activityId, LocalDateTime sendTime) {
        this.userId = userId;
        this.activityId = activityId;
        this.sendTime = sendTime;
    }
}