// filePath: ActivityRegistrationService.java
package com.example.springboot.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.ActivityRegistration;
import java.util.List;

public interface ActivityRegistrationService extends IService<ActivityRegistration> {
    // 根据用户ID查询报名的活动
    List<ActivityRegistration> getRegistrationsByUserId(Long userId);
}