package com.example.springboot.dto;

import lombok.Data;

/**
 * 搜索联想DTO：仅返回活动标题（供前端下拉选择）
 */
@Data
public class ActivitySuggestDTO {
    private String activityName; // 活动标题（核心联想字段）
}