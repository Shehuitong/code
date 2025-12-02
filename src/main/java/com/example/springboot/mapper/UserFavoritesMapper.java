package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.UserFavorites;
import org.apache.ibatis.annotations.Delete;
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
    // 1. 修复查询已收藏方法的参数类型和SQL字段
    @Select("select * from user_favorites where user_id = #{userId} and target_id = #{targetId} and target_type = #{targetType} limit 1")
    UserFavorites selectByUserIdTargetIdAndType(
            @Param("userId") Long userId,
            @Param("targetId") Long targetId,
            @Param("targetType") String targetType,  // 改为String类型，与实体类一致
            @Param("status") String status
    );

    // 2. 修复取消收藏（删除）方法的参数类型和SQL字段
    @Delete("delete from user_favorites where user_id = #{userId} and target_id = #{targetId} and target_type = #{targetType}")
    void deleteByUserIdTargetIdAndType(
            @Param("userId") Long userId,
            @Param("targetId") Long targetId,
            @Param("targetType") String targetType  // 改为String类型，与实体类一致
    );

    @Select("SELECT COUNT(DISTINCT user_id) FROM user_favorites " +
            "WHERE target_id = #{departmentId} " +
            "AND target_type = #{targetType} " +
            "AND favorite_status = #{status} " +
            "AND is_deleted = 0")
    Integer countByTargetIdAndType(
            @Param("departmentId") Long departmentId,
            @Param("targetType") String targetType,
            @Param("status") String status
    );
}