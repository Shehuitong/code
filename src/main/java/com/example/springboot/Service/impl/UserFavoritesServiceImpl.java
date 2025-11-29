package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.dto.ActivityDetailDTO;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.Department;
import com.example.springboot.entity.UserFavorites;
import com.example.springboot.excption.BusinessErrorException;
import com.example.springboot.mapper.DepartmentMapper;
import com.example.springboot.mapper.UserFavoritesMapper;
import com.example.springboot.service.ActivityService;
import com.example.springboot.service.DepartmentService;
import com.example.springboot.service.UserFavoritesService;
import com.example.springboot.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserFavoritesServiceImpl extends ServiceImpl<UserFavoritesMapper, UserFavorites> implements UserFavoritesService {

    private final UserService userService;
    private final DepartmentService departmentService; // 注入部门服务
    private final ActivityService activityService;     // 注入活动服务
    private final DepartmentMapper departmentMapper;
    // 构造器注入所有依赖
    @Autowired
    public UserFavoritesServiceImpl(UserService userService,
                                    DepartmentService departmentService,
                                    ActivityService activityService,DepartmentMapper departmentMapper) {
        this.userService = userService;
        this.departmentService = departmentService;
        this.activityService = activityService;
        this.departmentMapper = departmentMapper;
    }

    // 原方法：获取部门ID列表
    @Override
    public List<Long> getFavoriteDepartments(Long userId) {
        if (userService.getById(userId) == null) {
            throw new BusinessErrorException("用户不存在");
        }
        List<UserFavorites> favorites = baseMapper.selectByUserIdAndType(userId, UserFavorites.TYPE_DEPARTMENT);
        return favorites.stream()
                .map(UserFavorites::getTargetId)
                .collect(Collectors.toList());
    }

    // 原方法：获取活动ID列表
    @Override
    public List<Long> getFavoriteActivities(Long userId) {
        if (userService.getById(userId) == null) {
            throw new BusinessErrorException("用户不存在");
        }
        List<UserFavorites> favorites = baseMapper.selectByUserIdAndType(userId, UserFavorites.TYPE_ACTIVITY);
        return favorites.stream()
                .map(UserFavorites::getTargetId)
                .collect(Collectors.toList());
    }

    // 新增：获取部门详情列表
    @Override
    public List<Department> getFavoriteDepartmentsInfo(Long userId) {
        // 1. 验证用户存在性
        if (userService.getById(userId) == null) {
            throw new BusinessErrorException("用户不存在");
        }
        // 2. 获取收藏的部门ID列表
        List<Long> deptIds = getFavoriteDepartments(userId);
        // 3. 批量查询部门详情（依赖DepartmentService的listByIds方法）
        return departmentService.listByIds(deptIds);
    }

    // 新增：获取活动详情列表
    @Override
    public List<ActivityDetailDTO> getFavoriteActivitiesInfo(Long userId) {
        if (userService.getById(userId) == null) {
            throw new BusinessErrorException("用户不存在");
        }
        List<Long> activityIds = getFavoriteActivities(userId);
        List<Activity> activities = activityService.listByIds(activityIds);

        // 提取活动关联的部门ID
        List<Long> deptIds = activities.stream()
                .map(Activity::getDepartmentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 查询部门名称映射
        Map<Long, String> deptNameMap = deptIds.isEmpty() ? new HashMap<>() :
                departmentMapper.selectBatchIds(deptIds).stream()
                        .collect(Collectors.toMap(
                                Department::getDeptId,
                                Department::getDepartmentName,
                                (k1, k2) -> k1
                        ));

        // 转换为ActivityDetailDTO（包含部门名称）
        return activities.stream()
                .map(activity -> {
                    ActivityDetailDTO dto = new ActivityDetailDTO();
                    BeanUtils.copyProperties(activity, dto);
                    // 设置部门名称
                    dto.setDepartmentName(deptNameMap.get(activity.getDepartmentId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public int countFavoriteDepartments(Long userId) {
        if (userService.getById(userId) == null) {
            throw new BusinessErrorException("用户不存在");
        }
        return baseMapper.countByUserIdAndTargetType(userId, UserFavorites.TYPE_DEPARTMENT);
    }

    @Override
    public int countFavoriteActivities(Long userId) {
        if (userService.getById(userId) == null) {
            throw new BusinessErrorException("用户不存在");
        }
        return baseMapper.countByUserIdAndTargetType(userId, UserFavorites.TYPE_ACTIVITY);
    }
}
