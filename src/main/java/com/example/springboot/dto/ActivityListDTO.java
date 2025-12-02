package com.example.springboot.dto;

import com.example.springboot.entity.Department;
import com.example.springboot.enums.ActivityStatusEnum;
import com.example.springboot.enums.ApplyCollegeEnum;
import com.example.springboot.enums.GradeEnum;
import com.example.springboot.enums.HoldCollegeEnum;
import com.example.springboot.enums.ScoreTypeEnum;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 搜索列表DTO：包含活动所有属性
 */
@Data
public class ActivityListDTO {
    private Long activityId;         // 活动ID
    private String activityName;     // 活动标题
    private Long departmentId;       // 所属部门ID
    private String activityDesc;     // 活动详细介绍
    private LocalDateTime holdStartTime; // 活动开始时间
    private LocalDateTime holdEndTime;   // 活动结束时间
    private String location;         // 举办地点
    private ApplyCollegeEnum applyCollege; // 报名对象学院
    private GradeEnum applyGrade;    // 报名对象年级
    private ScoreTypeEnum scoreType; // 加分类型
    private BigDecimal score;        // 加分分值
    private Integer maxPeople;       // 总名额
    private Integer remainingPeople; // 剩余名额
    private Integer applyCount;      // 已报名人数
    private LocalDateTime applyDeadline; // 报名截止时间
    private ActivityStatusEnum status; // 活动状态
    private LocalDateTime createdTime; // 活动发布时间
    private LocalDateTime applyTime; // 报名开始时间
    private BigDecimal volunteerHours; // 志愿时长
    private Integer followerCount;   // 收藏人数
    private HoldCollegeEnum holdCollege; // 承办学院
    private Department department;
}