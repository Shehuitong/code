package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.dto.ActivityDetailDTO;
import com.example.springboot.dto.ActivityListDTO;
import com.example.springboot.dto.ActivitySuggestDTO;

import java.util.List;

/**
 * 活动搜索服务接口
 */
public interface ActivitySearchService {
    // 1. 搜索联想：根据关键词返回活动标题列表
    List<ActivitySuggestDTO> getSearchSuggest(String keyword);

    // 2. 搜索结果分页：根据关键词返回活动列表（带分页）
    List<ActivityListDTO> searchActivityList(String keyword);

    // 3. 活动详情：根据活动ID返回完整信息
    ActivityDetailDTO getActivityDetail(Long activityId);
    /**
     * 4. 获取所有未下架活动（带分页和部门信息）
     */
    List<ActivityDetailDTO> getAllActiveActivities();
}