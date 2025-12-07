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
    public void sendNotification(Long userId, String content,Long relatedActivityId,  // 新增：关联活动ID
                                 Long relatedDepartmentId,String notifyType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setContent(content);
        // 自动复制关联ID到实体中
        notification.setRelatedActivityId(relatedActivityId);  // 复制活动ID
        notification.setRelatedDepartmentId(relatedDepartmentId); // 复制部门ID
        notification.setSendTime(LocalDateTime.now());
        notification.setNotifyType(notifyType); // 设置通知类型
        notificationMapper.insert(notification);
    }

    // 查询用户的通知列表
    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationMapper.getByUserId(userId);
    }
}