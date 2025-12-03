package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.dto.ActivityEditDTO;
import com.example.springboot.dto.ActivityPublishDTO;
import com.example.springboot.entity.Activity;
import java.util.List;

public interface ActivityService extends IService<Activity> {
    // 根据部门ID查询活动列表
    List<Activity> getByDepartmentId(Long departmentId);

    // 发布新活动
    Activity publishActivity(ActivityPublishDTO activityDTO, Long departmentId);
    // 新增：统计部门发布的活动数量
    long countByDepartmentId(Long departmentId);
    //编辑活动信息
    Activity editActivity(Long activityId, ActivityEditDTO activityEditDTO, Long departmentId);
    // 添加下架活动的方法
    Activity offlineActivity(Long activityId, Long departmentId);
}