package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Notification;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 关联查询用户的通知（包含活动/部门详情，解决“关联不上”的核心）
     */
    @Select("""
        SELECT 
            n.notification_id AS notificationId,
            n.user_id AS userId,
            n.content AS content,
            n.send_time AS createTime,
            -- 关联活动表
            a.activity_id AS activityId,
            a.activity_name AS activityName,
            -- 关联部门表
            d.department_id AS departmentId,
            d.department_name AS departmentName
        FROM Notification n
        LEFT JOIN Activity a ON n.related_activity_id = a.activity_id
        LEFT JOIN Department d ON n.related_department_id = d.department_id
        WHERE n.user_id = #{userId}
        ORDER BY n.send_time DESC
        """)
    List<Notification> getByUserId(Long userId);
}