package com.example.springboot.service.impl;

import com.example.springboot.entity.Notification;
import com.example.springboot.mapper.NotificationMapper;
import com.example.springboot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    // 发送通知（定时任务触发时调用）
    @Override
    public void sendNotification(Long userId, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setContent(content);
        notification.setCreateTime(LocalDateTime.now());
        notification.setIsRead(0);  // 初始为未读
        notificationMapper.insert(notification);
    }

    // 查询用户的通知列表
    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationMapper.getByUserId(userId);
    }
}