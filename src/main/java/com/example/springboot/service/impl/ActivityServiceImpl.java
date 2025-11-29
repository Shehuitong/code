package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Activity;
import com.example.springboot.mapper.ActivityMapper;
import com.example.springboot.service.ActivityService;
import org.springframework.stereotype.Service;
import java.util.List;

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
}