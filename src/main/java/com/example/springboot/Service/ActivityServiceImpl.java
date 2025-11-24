package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Activity;
import com.example.springboot.mapper.ActivityMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Override
    public List<Activity> getByDepartmentId(int departmentId) {
        return baseMapper.selectList(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getDepartmentId, departmentId)
                        .orderByDesc(Activity::getCreatedTime) // 按发布时间倒序
        );
    }
}