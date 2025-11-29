package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.UserFavorites;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFavoritesMapper extends BaseMapper<UserFavorites> {
    // 统计用户某类型收藏数（参数改为 targetType，数据库字段 target_type）
    @Select("SELECT COUNT(*) FROM user_favorites WHERE user_id = #{userId} AND target_type = #{targetType}")
    Integer countByUserIdAndTargetType(
            @Param("userId") Long userId,
            @Param("targetType") String targetType
    );

    // 注解SQL：查询用户某类型收藏记录（直接写SQL，无需XML）
    @Select("SELECT * FROM user_favorites WHERE user_id = #{userId} AND target_type = #{targetType} ORDER BY create_time DESC")
    // 根据用户ID和目标类型查询收藏记录
    default List<UserFavorites> selectByUserIdAndType(Long userId, String targetType) {
        return selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFavorites>()
                        .eq(UserFavorites::getUserId, userId)
                        .eq(UserFavorites::getTargetType, targetType)
                        .eq(UserFavorites::getFavoriteStatus, UserFavorites.STATUS_FAVORITED)
                        .eq(UserFavorites::getIsDeleted, 0)
        );
    }
}