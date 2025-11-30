package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.dto.ActivityRegistrationDTO;
import com.example.springboot.entity.ActivityRegistration;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
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

    /**
     * 导出指定活动的已报名用户信息为Excel
     * @param activityId 活动ID
     * @param response 响应对象（用于输出Excel流）
     * @throws IOException IO异常（流操作失败）
     * @throws RuntimeException 业务异常（活动不存在、无报名用户等）
     */
    void exportRegisteredUsers(Long activityId, HttpServletResponse response) throws IOException;

}