package com.example.springboot.service;

import com.example.springboot.entity.Notification;
import java.util.List;

public interface NotificationService {
    // 发送通知（被定时任务调用）
    void sendNotification(Long userId, String content);

    // 查询用户的通知列表
    List<Notification> getUserNotifications(Long userId);
}