package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.dto.ActivityPublishDTO;
import com.example.springboot.entity.Activity;
import com.example.springboot.enums.ActivityStatusEnum;
import com.example.springboot.mapper.ActivityMapper;
import com.example.springboot.service.ActivityService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDateTime;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Override
    public List<Activity> getByDepartmentId(Long departmentId) {
        return baseMapper.selectList(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getDepartmentId, departmentId)
                        .orderByDesc(Activity::getCreatedTime) // 按发布时间倒序
        );
    }

    @Override
    public Activity publishActivity(ActivityPublishDTO activityDTO, Long departmentId) {
        // 验证时间逻辑
        validateTime(activityDTO);

        // 转换DTO为实体对象
        Activity activity = new Activity();
        activity.setActivityName(activityDTO.getActivityName());
        activity.setDepartmentId(departmentId);
        activity.setActivityDesc(activityDTO.getActivityDesc());
        activity.setHoldStartTime(activityDTO.getHoldStartTime());
        activity.setHoldEndTime(activityDTO.getHoldEndTime());
        activity.setLocation(activityDTO.getLocation());
        activity.setApplyCollege(activityDTO.getApplyCollege());
        activity.setScoreType(activityDTO.getScoreType());
        activity.setScore(activityDTO.getScore());
        activity.setMaxPeople(activityDTO.getMaxPeople());
        // 初始剩余名额等于最大人数
        activity.setRemainingPeople(activityDTO.getMaxPeople());
        // 初始报名人数为0
        activity.setApplyCount(0);
        activity.setApplyDeadline(activityDTO.getApplyDeadline());
        // 根据初始状态：如果报名开始时间在当前时间之后，设置为"报名未开始"，否则"报名进行中"
        LocalDateTime now = LocalDateTime.now();
        if (activityDTO.getApplyTime().isAfter(now)) {
            activity.setStatus(ActivityStatusEnum.NOT_STARTED);
        } else {
            activity.setStatus(ActivityStatusEnum.IN_PROGRESS);
        }
        activity.setCreatedTime(now);
        activity.setApplyTime(activityDTO.getApplyTime());
        activity.setVolunteerHours(activityDTO.getVolunteerHours());
        // 初始收藏人数为0
        activity.setFollowerCount(0);
        activity.setHoldCollege(activityDTO.getHoldCollege());
        activity.setApplyGrade(activityDTO.getApplyGrade());

        // 保存活动信息
        baseMapper.insert(activity);
        return activity;
    }

    // 验证时间逻辑
    private void validateTime(ActivityPublishDTO dto) {
        LocalDateTime now = LocalDateTime.now();

        // 活动开始时间必须在当前时间之后
        if (dto.getHoldStartTime().isBefore(now)) {
            throw new IllegalArgumentException("活动开始时间不能早于当前时间");
        }

        // 活动结束时间必须在活动开始时间之后
        if (dto.getHoldEndTime().isBefore(dto.getHoldStartTime())) {
            throw new IllegalArgumentException("活动结束时间不能早于活动开始时间");
        }

        // 报名开始时间必须在当前时间之后
        if (dto.getApplyTime().isBefore(now)) {
            throw new IllegalArgumentException("报名开始时间不能早于当前时间");
        }

        // 报名截止时间必须在报名开始时间之后
        if (dto.getApplyDeadline().isBefore(dto.getApplyTime())) {
            throw new IllegalArgumentException("报名截止时间不能早于报名开始时间");
        }

        // 报名截止时间必须在活动开始时间之前
        if (dto.getApplyDeadline().isAfter(dto.getHoldStartTime())) {
            throw new IllegalArgumentException("报名截止截止时间不能晚于活动开始时间");
        }
    }
    // 新增：实现统计部门活动数的方法
    @Override
    public long countByDepartmentId(Long departmentId) {
        return baseMapper.selectCount(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getDepartmentId, departmentId)
        );
    }
}