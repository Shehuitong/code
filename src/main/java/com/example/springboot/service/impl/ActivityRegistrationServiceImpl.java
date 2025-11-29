package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.dto.ActivityDetailDTO;
import com.example.springboot.dto.ActivityRegistrationDTO;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.ActivityRegistration;
import com.example.springboot.entity.Department;
import com.example.springboot.enums.RegistrationStatusEnum;
import com.example.springboot.excption.BusinessErrorException;
import com.example.springboot.mapper.ActivityMapper;
import com.example.springboot.mapper.ActivityRegistrationMapper;
import com.example.springboot.mapper.DepartmentMapper;
import com.example.springboot.service.ActivityRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActivityRegistrationServiceImpl extends ServiceImpl<ActivityRegistrationMapper, ActivityRegistration>
        implements ActivityRegistrationService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    /**
     * 查看用户已报名的活动（仅查状态码1，返回活动全部信息）
     * @param userId 用户ID
     * @return 已报名活动DTO列表（含报名信息+完整活动信息+部门信息）
     */
    @Override
    public List<ActivityRegistrationDTO> getMyRegisteredActivities(Long userId) {
        // 1. 基础校验
        if (userId == null) {
            log.error("查看已报名活动失败：用户ID为空（Token解析异常）");
            throw new BusinessErrorException("登录状态异常，请重新登录");
        }

        // 2. 查询用户「已报名」状态的报名记录（状态码1）
        LambdaQueryWrapper<ActivityRegistration> registrationWrapper = new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getUserId, userId.toString())
                .eq(ActivityRegistration::getRegistrationStatus, RegistrationStatusEnum.APPLIED);

        List<ActivityRegistration> registrationList = baseMapper.selectList(registrationWrapper);
        if (registrationList.isEmpty()) {
            log.info("用户{}暂无已报名的活动", userId);
            return Collections.emptyList();
        }

        // 3. 批量查询关联的活动信息
        Set<Long> activityIds = registrationList.stream()
                .map(ActivityRegistration::getActivityId)
                .collect(Collectors.toSet());
        List<Activity> activityList = activityMapper.selectActivityWithDeptByIds(new ArrayList<>(activityIds));
        Map<Long, Activity> activityMap = activityList.stream()
                .collect(Collectors.toMap(Activity::getActivityId, activity -> activity));

        // 4. 批量查询活动关联的部门信息
        Set<Long> departmentIds = activityList.stream()
                .map(Activity::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<Department> departmentList = departmentMapper.selectDeptWithName(new ArrayList<>(departmentIds));
        Map<Long, Department> departmentMap = departmentList.stream()
                .collect(Collectors.toMap(Department::getDeptId, department -> department));

        // 5. 组装DTO：报名信息 + 嵌套完整活动信息（ActivityDetailDTO）
        List<ActivityRegistrationDTO> resultDTOs = new ArrayList<>();
        for (ActivityRegistration registration : registrationList) {
            ActivityRegistrationDTO registrationDTO = new ActivityRegistrationDTO();
            // 设置报名记录ID
            registrationDTO.setRegistrationId(registration.getRegistrationId());
            // 设置报名状态（枚举类型）
            registrationDTO.setRegistrationStatus(RegistrationStatusEnum.APPLIED);

            // 转换活动信息为ActivityDetailDTO
            Activity activity = activityMap.get(registration.getActivityId());
            if (activity != null) {
                ActivityDetailDTO activityDetailDTO = new ActivityDetailDTO();
                BeanUtils.copyProperties(activity, activityDetailDTO);

                // 补充部门名称到活动详情
                Department department = departmentMap.get(activity.getDepartmentId());
                activityDetailDTO.setDepartmentName(department != null ? department.getDepartmentName() : "未知部门");

                // 将活动详情设置到报名DTO中
                registrationDTO.setActivity(activityDetailDTO);
            } else {
                // 处理活动已删除的情况
                ActivityDetailDTO deletedActivity = new ActivityDetailDTO();
                deletedActivity.setActivityName("该活动已删除");
                registrationDTO.setActivity(deletedActivity);
                log.warn("用户{}的报名记录（ID：{}）关联的活动（ID：{}）已删除",
                        userId, registration.getRegistrationId(), registration.getActivityId());
            }

            resultDTOs.add(registrationDTO);
        }

        log.info("用户{}查询已报名活动成功，共{}条记录", userId, resultDTOs.size());
        return resultDTOs;
    }

    @Override
    public List<ActivityRegistrationDTO> getUserRegistrationDTOs(Long userId) {
        return getMyRegisteredActivities(userId);
    }

    @Override
    public int countByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessErrorException("用户ID不能为空");
        }

        LambdaQueryWrapper<ActivityRegistration> countWrapper = new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getUserId, userId.toString())
                .eq(ActivityRegistration::getRegistrationStatus, RegistrationStatusEnum.APPLIED);

        return Math.toIntExact(baseMapper.selectCount(countWrapper));
    }

    @Override
    public List<ActivityRegistration> getRegistrationsByUserId(Long userId) {
        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getUserId, userId);
        return baseMapper.selectList(wrapper);
    }
}