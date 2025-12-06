package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Notification;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface NotificationMapper extends BaseMapper<Notification> {
    // 根据用户ID查询通知
    @Select("SELECT * FROM Notification WHERE user_id = #{userId} ORDER BY send_time DESC")
    List<Notification> getByUserId(Long userId);
}