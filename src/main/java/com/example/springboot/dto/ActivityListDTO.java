package com.example.springboot.dto;

import com.example.springboot.enums.ActivityStatusEnum;
import com.example.springboot.enums.ApplyCollegeEnum;
import com.example.springboot.enums.GradeEnum;
import com.example.springboot.enums.ScoreTypeEnum;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 搜索列表DTO：列表页显示的活动信息
 */
@Data
public class ActivityListDTO {
    private Long activityId;         // 活动ID（用于跳转详情）
    private String activityName;     // 活动标题
    private Long departmentId;    // 所属部门ID（可关联部门名）
    private LocalDateTime holdStartTime; // 活动开始时间
    private String location;         // 举办地点
    private ApplyCollegeEnum applyCollege; // 报名对象学院
    private GradeEnum applyGrade;    // 报名对象年级
    private ScoreTypeEnum scoreType; // 加分类型（可选）
    private BigDecimal score;        // 加分分值（可选）
    private Integer remainingPeople; // 剩余名额（用户关心）
    private ActivityStatusEnum status; // 活动状态（报名中/截止）
    private LocalDateTime applyDeadline; // 报名截止时间
}