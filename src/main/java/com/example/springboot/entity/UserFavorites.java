package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_favorites")
public class UserFavorites {
    @TableId(value = "favorite_id", type = IdType.AUTO)
    private Long favoriteId;

    @TableField("user_id")
    private Long userId;

    @TableField("target_id")
    private Long targetId;

    @TableField("target_type")
    private String targetType;

    @TableField("favorite_status")
    private String favoriteStatus;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    @TableLogic
    private Integer isDeleted;

    public static final String TYPE_ACTIVITY = "ACTIVITY";
    public static final String TYPE_DEPARTMENT = "DEPARTMENT";
    public static final String STATUS_FAVORITED = "已收藏";
    public static final String STATUS_CANCELLED = "已取消";

    public UserFavorites() {
        this.createTime = LocalDateTime.now();
        this.favoriteStatus = STATUS_FAVORITED;
        this.isDeleted = 0;
    }
}