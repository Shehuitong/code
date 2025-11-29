package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.dto.ActivityRegistrationDTO;
import com.example.springboot.entity.ActivityRegistration;
import java.util.List;

/**
 * 活动报名服务接口
 */
public interface ActivityRegistrationService extends IService<ActivityRegistration> {

    List<ActivityRegistration> getRegistrationsByUserId(Long userId);

    /**
     * 查询当前用户已报名的活动（含部门信息）
     * @param userId 用户ID
     * @return 报名活动列表（带部门ID+名称）
     */
    List<ActivityRegistrationDTO> getMyRegisteredActivities(Long userId);
    int countByUserId(Long userId);

    List<ActivityRegistrationDTO> getUserRegistrationDTOs(Long currentUserId);
}