package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.dto.ActivityPublishDTO;
import com.example.springboot.entity.Activity;
import java.util.List;

public interface ActivityService extends IService<Activity> {
    // 根据部门ID查询活动列表
    List<Activity> getByDepartmentId(Long departmentId);

    // 发布新活动
    Activity publishActivity(ActivityPublishDTO activityDTO, Long departmentId);
}