// filePath: ActivityRegistrationMapper.java
package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ActivityRegistrationMapper extends BaseMapper<ActivityRegistration> {
    // 根据用户ID查询报名记录（关联活动信息）
    List<ActivityRegistration> selectByUserIdWithActivity(@Param("userId") Long userId);
}