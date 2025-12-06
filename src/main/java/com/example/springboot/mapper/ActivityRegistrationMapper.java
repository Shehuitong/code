// filePath: ActivityRegistrationMapper.java
package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ActivityRegistrationMapper extends BaseMapper<ActivityRegistration> {
    @Select("SELECT " +
            "r.registration_id, " +
            "r.user_id AS userId, " +  // 表中列是id，映射到实体类的userId
            "r.activity_id, " +
            "r.registration_status, " +
            "a.activity_name, " +
            "a.hold_start_time, " +
            "a.location " +
            "FROM UserActivityRegistration r " +
            "LEFT JOIN Activity a ON r.activity_id = a.activity_id " +
            "WHERE r.user_id = #{userId}")  // 条件字段从 user_id 改为 id
        // 根据用户ID查询报名记录（关联活动信息）
    List<ActivityRegistration> selectByUserIdWithActivity(@Param("userId") Long userId);
}