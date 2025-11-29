package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.springboot.enums.ActivityStatusEnum;
import com.example.springboot.enums.ApplyCollegeEnum;
import com.example.springboot.enums.GradeEnum;
import com.example.springboot.enums.HoldCollegeEnum;
import com.example.springboot.enums.ScoreTypeEnum;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动实体类（对应数据库Activity表）
 */
@Data
@TableName("Activity")
public class Activity {
    @TableId(value = "activity_id", type = IdType.AUTO)
    private Long activityId; // 活动主键ID

    @TableField(value = "activity_name", insertStrategy = FieldStrategy.NOT_EMPTY)
    private String activityName; // 活动名称（非空）

    @TableField(value = "department_id", insertStrategy = FieldStrategy.NOT_NULL)
    private Long departmentId; // 关联部门ID（外键，非空）

    @TableField(value = "activity_desc",insertStrategy = FieldStrategy.NOT_EMPTY)
    private String activityDesc; // 活动详细介绍（非空）

    @TableField(value = "hold_start_time",insertStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime holdStartTime; // 活动开始时间（非空）

    @TableField(value = "hold_end_time", insertStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime holdEndTime; // 活动结束时间（非空）

    @TableField(value = "location",insertStrategy = FieldStrategy.NOT_EMPTY)
    private String location; // 举办地点（非空）

    @TableField(value = "apply_college", insertStrategy = FieldStrategy.NOT_NULL)
    private ApplyCollegeEnum applyCollege; // 报名对象学院（非空，枚举）

    @TableField(value = "score_type")
    private ScoreTypeEnum scoreType; // 加分类型（可选，枚举）

    @TableField(value = "score")
    private BigDecimal score; // 加分分值（可选）

    @TableField(value = "max_people",insertStrategy = FieldStrategy.NOT_NULL)
    private Integer maxPeople; // 人数限制（0表示无限制，非空）

    @TableField(value = "remaining_people",insertStrategy = FieldStrategy.NOT_NULL)
    private Integer remainingPeople; // 剩余名额（非空）

    @TableField(value = "apply_count", insertStrategy = FieldStrategy.NOT_NULL)
    private Integer applyCount; // 已报名人数（非空）

    @TableField(value = "apply_deadline", insertStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime applyDeadline; // 报名截止时间（非空）

    @TableField(value = "status", insertStrategy = FieldStrategy.NOT_NULL)
    private ActivityStatusEnum status; // 活动状态（非空，枚举）

    @TableField(value = "created_time", fill = FieldFill.INSERT, insertStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime createdTime; // 活动发布时间（非空，默认当前时间）

    @TableField(value = "apply_time", insertStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime applyTime; // 报名开始时间（非空）

    @TableField(value = "volunteer_hours")
    private BigDecimal volunteerHours; // 志愿时长（可选）

    @TableField(value = "follower_count")
    private Integer followerCount; // 收藏人数（可选）

    @TableField(value = "hold_college", insertStrategy = FieldStrategy.NOT_NULL)
    private HoldCollegeEnum holdCollege; // 活动承办学院（非空，枚举）

    @TableField(value = "apply_grade", insertStrategy = FieldStrategy.NOT_NULL)
    private GradeEnum applyGrade; // 报名对象年级（非空，复用GradeEnum）

    // 非数据库字段：关联查询部门信息
    @TableField(exist = false)
    private Department department;


}