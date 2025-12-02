package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.ReminderLog;
import com.example.springboot.entity.UserFavorites;
import com.example.springboot.enums.ActivityStatusEnum;
import com.example.springboot.mapper.ReminderLogMapper;
import com.example.springboot.service.ActivityService;
import com.example.springboot.service.NotificationService;
import com.example.springboot.service.UserFavoritesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 活动报名提醒定时任务：在活动报名开始前5分钟，向收藏该活动的用户发送固定通知
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityReminderTask {

    private final ActivityService activityService;
    private final UserFavoritesService userFavoritesService;
    private final NotificationService notificationService;
    private final ReminderLogMapper reminderLogMapper;

    /**
     * 定时任务：每10秒执行一次
     */
    @Scheduled(cron = "*/10 * * * * ?")
    public void checkAndSendReminder() {
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime targetReminderTime = currentTime.plusMinutes(5); // 报名前5分钟提醒

            // 1. 查询符合条件的活动（使用枚举的desc字段，兼容状态值）
            LambdaQueryWrapper<Activity> activityQuery = new LambdaQueryWrapper<>();
            activityQuery.in(Activity::getStatus,
                            ActivityStatusEnum.NOT_STARTED.getDesc(), // 替换getCode()为getDesc()
                            ActivityStatusEnum.IN_PROGRESS.getDesc()) // 用枚举替代硬编码的"报名进行中"
                    .between(Activity::getApplyTime,
                            targetReminderTime.minusMinutes(1),  // 时间窗口放宽到1分钟
                            targetReminderTime.plusMinutes(1))
                    .gt(Activity::getApplyTime, currentTime);

            List<Activity> targetActivities = activityService.list(activityQuery);
            if (targetActivities.isEmpty()) {
                log.debug("当前无需要发送提醒的活动（时间窗口：{}）", targetReminderTime);
                return;
            }

            // 2. 按活动ID分组，便于后续关联
            Map<Long, Activity> activityMap = targetActivities.stream()
                    .collect(Collectors.toMap(Activity::getActivityId, a -> a));
            Set<Long> activityIds = activityMap.keySet();
            log.debug("检测到需要提醒的活动：{}", activityIds);

            // 3. 查询收藏了这些活动的用户（已收藏且未删除）
            LambdaQueryWrapper<UserFavorites> favoritesQuery = new LambdaQueryWrapper<>();
            favoritesQuery.eq(UserFavorites::getTargetType, "ACTIVITY")
                    .eq(UserFavorites::getFavoriteStatus, "已收藏")
                    .eq(UserFavorites::getIsDeleted, 0)
                    .in(UserFavorites::getTargetId, activityIds);

            List<UserFavorites> userFavorites = userFavoritesService.list(favoritesQuery);
            if (userFavorites.isEmpty()) {
                log.debug("无用户收藏需要提醒的活动，无需发送通知");
                return;
            }

            // 4. 按【用户ID+活动ID】分组，避免同一活动重复提醒
            Map<Long, Map<Long, UserFavorites>> userActivityMap = userFavorites.stream()
                    .collect(Collectors.groupingBy(
                            UserFavorites::getUserId,
                            Collectors.toMap(UserFavorites::getTargetId, f -> f)
                    ));

            // 5. 遍历发送通知（按用户+活动去重）
            String notificationContent = "您收藏的活动距离报名开始还有五分钟！";
            for (Map.Entry<Long, Map<Long, UserFavorites>> userEntry : userActivityMap.entrySet()) {
                Long userId = userEntry.getKey();
                Map<Long, UserFavorites> activityFavorites = userEntry.getValue();

                for (Long activityId : activityFavorites.keySet()) {
                    // 校验：24小时内同一用户+活动是否已提醒
                    if (isRemindedIn24Hours(userId, activityId)) {
                        log.info("用户[{}]24小时内已收到活动[{}]的提醒，本次跳过", userId, activityId);
                        continue;
                    }

                    // 发送通知
                    notificationService.sendNotification(userId, notificationContent);
                    // 记录日志（关联用户+活动）
                    recordReminderLog(userId, activityId);
                    log.info("已向用户[{}]发送活动[{}]的报名提醒", userId, activityId);
                }
            }

        } catch (Exception e) {
            log.error("活动报名提醒任务执行失败", e);
        }
    }

    /**
     * 校验：24小时内同一用户+活动是否已发送提醒
     */
    private boolean isRemindedIn24Hours(Long userId, Long activityId) {
        LambdaQueryWrapper<ReminderLog> logQuery = new LambdaQueryWrapper<>();
        logQuery.eq(ReminderLog::getUserId, userId)
                .eq(ReminderLog::getActivityId, activityId) // 关联活动ID
                .ge(ReminderLog::getSendTime, LocalDateTime.now().minusHours(24));
        return reminderLogMapper.selectCount(logQuery) > 0;
    }

    /**
     * 记录提醒日志（关联用户+活动）
     */
    private void recordReminderLog(Long userId, Long activityId) {
        ReminderLog reminderLog = new ReminderLog();
        reminderLog.setUserId(userId);
        reminderLog.setActivityId(activityId); // 关联活动ID
        reminderLog.setSendTime(LocalDateTime.now());
        reminderLogMapper.insert(reminderLog);
    }
}