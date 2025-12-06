
package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.dto.ActivityDetailDTO;
import com.example.springboot.entity.Department;
import com.example.springboot.entity.UserFavorites;
import java.util.List;

public interface UserFavoritesService extends IService<UserFavorites> {

    // 查看用户收藏的部门
    List<Long> getFavoriteDepartments(Long userId);

    // 查看用户关注的活动
    List<Long> getFavoriteActivities(Long userId);
    // 新增：查看用户收藏的部门详情列表
    List<Department> getFavoriteDepartmentsInfo(Long userId);

    // 新增：查看用户关注的活动详情列表
    List<ActivityDetailDTO> getFavoriteActivitiesInfo(Long userId);
    // 统计用户收藏的部门数量
    int countFavoriteDepartments(Long userId);

    // 统计用户收藏的活动数量
    int countFavoriteActivities(Long userId);
    // 收藏（参数：目标ID、类型，返回完整收藏信息）
    UserFavorites addFavorite(Long targetId, String targetType);

    // 取消收藏（参数：目标ID、类型，返回完整收藏信息）
    UserFavorites cancelFavorite(Long targetId, String targetType);
    /**
     * 统计部门的关注人数
     * @param departmentId 部门ID
     * @return 关注人数
     */
    int countDepartmentFollowers(Long departmentId);
    // 新增：根据部门ID查询所有收藏该部门的用户ID
    List<Long> getUserIdsByFavoriteDepartment(Long departmentId);
    List<UserFavorites> getUserAllFavorites(Long userId);
}