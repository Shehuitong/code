package com.example.springboot.service;

import com.example.springboot.entity.Notification;
import java.util.List;

public interface NotificationService {
    // 发送通知（被定时任务调用）
    void sendNotification(Long userId, String content,
                          Long relatedActivityId,  // 新增：关联活动ID
                          Long relatedDepartmentId); // 新增：关联部门ID
    // 查询用户的通知列表
    List<Notification> getUserNotifications(Long userId);
}