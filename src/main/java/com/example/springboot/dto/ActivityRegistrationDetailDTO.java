package com.example.springboot.dto;

import com.example.springboot.entity.Activity;
import com.example.springboot.entity.ActivityRegistration;
import com.example.springboot.enums.RegistrationStatusEnum;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityRegistrationDetailDTO {
    // 报名记录ID
    private Long registrationId;
    // 活动ID
    private Long activityId;
    // 活动名称
    private String activityName;
    // 活动开始时间
    private LocalDateTime activityStartTime;
    // 活动结束时间
    private LocalDateTime activityEndTime;
    // 报名状态（枚举描述，如"已报名"、"已取消"）
    private String registrationStatusDesc;
    // 报名时间
    private LocalDateTime createTime;
    // 取消报名时间（可为空）
    private LocalDateTime cancelTime;

    // 从报名记录和活动信息组装DTO
    public static ActivityRegistrationDetailDTO from(ActivityRegistration registration, Activity activity) {
        ActivityRegistrationDetailDTO dto = new ActivityRegistrationDetailDTO();
        dto.setRegistrationId(registration.getRegistrationId());
        dto.setActivityId(activity.getActivityId());
        dto.setActivityName(activity.getActivityName());
        dto.setActivityStartTime(activity.getHoldStartTime()); // 对应Activity的holdStartTime字段
        dto.setActivityEndTime(activity.getHoldEndTime());     // 对应Activity的holdEndTime字段
        dto.setRegistrationStatusDesc(registration.getRegistrationStatus().getDesc());
        return dto;
    }
}