// filePath: com/example/springboot/service/impl/ActivityStatusScheduledService.java
package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.entity.Activity;
import com.example.springboot.enums.ActivityStatusEnum;
import com.example.springboot.mapper.ActivityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityStatusScheduledServiceImpl {

    private final ActivityMapper activityMapper;

    /**
     * 定时更新活动状态
     * 每分钟执行一次（可根据实际需求调整频率）
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void updateActivityStatus() {
        log.info("开始执行活动状态自动更新任务");
        LocalDateTime now = LocalDateTime.now();

        // 1. 将已到报名开始时间但未开始的活动改为"报名进行中"
        List<Activity> notStartedActivities = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, ActivityStatusEnum.NOT_STARTED)
                .le(Activity::getApplyTime, now));

        for (Activity activity : notStartedActivities) {
            activity.setStatus(ActivityStatusEnum.IN_PROGRESS);
            activityMapper.updateById(activity);
            log.info("活动[{}]状态更新为：报名进行中", activity.getActivityId());
        }

        // 2. 将已到报名截止时间但未截止的活动改为"报名截止"
        List<Activity> ongoingActivities = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .in(Activity::getStatus, ActivityStatusEnum.NOT_STARTED, ActivityStatusEnum.IN_PROGRESS)
                .le(Activity::getApplyDeadline, now));

        for (Activity activity : ongoingActivities) {
            activity.setStatus(ActivityStatusEnum.CLOSED);
            activityMapper.updateById(activity);
            log.info("活动[{}]状态更新为：报名截止", activity.getActivityId());
        }

        log.info("活动状态自动更新任务执行完成，共更新{}个活动",
                notStartedActivities.size() + ongoingActivities.size());
    }
}