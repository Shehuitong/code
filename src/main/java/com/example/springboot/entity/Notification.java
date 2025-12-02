package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知表：存储用户收到的提醒
 */
@Data
@TableName("notification")  // 数据库表名
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;  // 通知ID
    private Long userId;  // 接收通知的用户ID（关联user表id）
    private String content;  // 通知内容（固定为"您收藏的活动距离报名开始还有五分钟！"）
    private LocalDateTime createTime;  // 通知创建时间
    private Integer isRead;  // 是否已读（0=未读，1=已读）
}