package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.dto.ActivityRegistrationDTO;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.User;
import com.example.springboot.entity.ActivityRegistration;
import com.example.springboot.excption.BusinessErrorException;
import com.example.springboot.mapper.ActivityMapper;
import com.example.springboot.mapper.ActivityRegistrationMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.ActivityRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityRegistrationServiceImpl
        extends ServiceImpl<ActivityRegistrationMapper, ActivityRegistration>
        implements ActivityRegistrationService {

    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;

    @Autowired
    public ActivityRegistrationServiceImpl(UserMapper userMapper, ActivityMapper activityMapper) {
        this.userMapper = userMapper;
        this.activityMapper = activityMapper;
    }

    @Override
    public List<ActivityRegistration> getRegistrationsByUserId(Long userId) {
        // 1. 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessErrorException("用户不存在");
        }

        // 2. 查询该用户的所有报名记录（修复查询条件和语法错误）
        LambdaQueryWrapper<ActivityRegistration> wrapper =
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getUserId, userId); // 修复：使用userId字段匹配，而非id

        List<ActivityRegistration> registrations = baseMapper.selectList(wrapper); // 修复：添加查询语句，定义registrations变量

        // 3. 提取所有活动ID，批量查询活动详情
        List<Long> activityIds = registrations.stream()
                .map(ActivityRegistration::getActivityId)
                .distinct()
                .collect(Collectors.toList());

        List<Activity> activities = activityMapper.selectBatchIds(activityIds);

        // 4. 组装活动详情到报名记录中
        Map<Long, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getActivityId, activity -> activity));

        registrations.forEach(registration -> {
            Activity activity = activityMap.get(registration.getActivityId());
            registration.setActivity(activity);
        });

        // 5. 返回结果
        return registrations;
    }

    @Override
    public List<ActivityRegistrationDTO> getMyRegisteredActivities(Long userId) {
        return List.of();
    }

    @Override
    public int countByUserId(Long userId) {
        return 0;
    }

    @Override
    public List<ActivityRegistrationDTO> getUserRegistrationDTOs(Long currentUserId) {
        return List.of();
    }
}