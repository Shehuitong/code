package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.example.springboot.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy; // 正确包
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Service
public class UserFavoritesServiceImpl extends ServiceImpl<UserFavoritesMapper, UserFavorites> implements UserFavoritesService {

    private final UserService userService;
    private final DepartmentService departmentService; // 注入部门服务
    private final ActivityService activityService;     // 注入活动服务
    private final DepartmentMapper departmentMapper;
    private final JwtUtil jwtUtil;
    // 构造器注入所有依赖
    @Autowired
    public UserFavoritesServiceImpl(UserService userService,
                                    @Lazy DepartmentService departmentService,
                                    @Lazy ActivityService activityService,
                                    DepartmentMapper departmentMapper,
                                    JwtUtil jwtUtil) {
        this.userService = userService;
        this.departmentService = departmentService;
        this.activityService = activityService;
        this.departmentMapper = departmentMapper;
        this.jwtUtil = jwtUtil;
    }
    @Override
    public UserFavorites addActivityFavorite(Long activityId) {
        // 1. 获取当前用户ID和角色
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();

        // 2. 权限校验：管理员不能收藏活动
        if ("admin".equals(role)) {
            throw new BusinessErrorException("管理员不能收藏活动");
        }

        // 3. 校验活动是否存在
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            throw new BusinessErrorException("活动不存在");
        }

        // 4. 校验是否已收藏
        if (isAlreadyFavorited(userId, activityId, UserFavorites.TYPE_ACTIVITY)) {
            throw new BusinessErrorException("已收藏该活动，无需重复操作");
        }

        // 5. 保存收藏记录
        UserFavorites favorites = new UserFavorites();
        favorites.setUserId(userId);
        favorites.setTargetId(activityId);
        favorites.setTargetType(UserFavorites.TYPE_ACTIVITY);
        favorites.setFavoriteStatus(UserFavorites.STATUS_FAVORITED);
        baseMapper.insert(favorites);

        // 6. 更新活动关注数
        activity.setFollowerCount(activity.getFollowerCount() + 1);
        activityService.updateById(activity);
        boolean updateSuccess = activityService.updateById(activity);
        log.info("活动[{}]收藏数更新结果: {}", activityId, updateSuccess); // 新增日志

        return favorites;
    }

    @Override
    public UserFavorites cancelActivityFavorite(Long activityId) {
        // 1. 获取当前用户ID和角色
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();

        // 2. 权限校验：管理员不能取消收藏活动
        if ("admin".equals(role)) {
            throw new BusinessErrorException("管理员不能取消收藏活动");
        }

        // 3. 校验活动是否存在
        if (activityService.getById(activityId) == null) {
            throw new BusinessErrorException("活动不存在");
        }

        // 4. 校验是否已收藏
        UserFavorites exist = getExistingFavorite(userId, activityId, UserFavorites.TYPE_ACTIVITY);
        if (exist == null) {
            throw new BusinessErrorException("未收藏该活动，无法取消");
        }

        // 5. 更新收藏状态为取消
        exist.setFavoriteStatus(UserFavorites.STATUS_CANCELLED);
        baseMapper.updateById(exist);

        // 6. 更新活动关注数（减1）
        Activity activity = activityService.getById(activityId);
        activity.setFollowerCount(Math.max(0, activity.getFollowerCount() - 1)); // 避免负数
        activityService.updateById(activity);

        return exist;
    }

    // ------------------------------ 关注部门相关 ------------------------------
    @Override
    public UserFavorites addDepartmentFavorite(Long departmentId) {
        // 1. 获取当前用户ID
        Long userId = getCurrentUserId();

        // 2. 校验部门是否存在
        if (departmentService.getById(departmentId) == null) {
            throw new BusinessErrorException("部门不存在");
        }

        // 3. 校验是否已关注
        if (isAlreadyFavorited(userId, departmentId, UserFavorites.TYPE_DEPARTMENT)) {
            throw new BusinessErrorException("已关注该部门，无需重复操作");
        }

        // 4. 保存关注记录
        UserFavorites favorites = new UserFavorites();
        favorites.setUserId(userId);
        favorites.setTargetId(departmentId);
        favorites.setTargetType(UserFavorites.TYPE_DEPARTMENT);
        favorites.setFavoriteStatus(UserFavorites.STATUS_FAVORITED);
        baseMapper.insert(favorites);

        return favorites;
    }

    @Override
    public UserFavorites cancelDepartmentFavorite(Long departmentId) {
        // 1. 获取当前用户ID
        Long userId = getCurrentUserId();

        // 2. 校验部门是否存在
        if (departmentService.getById(departmentId) == null) {
            throw new BusinessErrorException("部门不存在");
        }

        // 3. 校验是否已关注
        UserFavorites exist = getExistingFavorite(userId, departmentId, UserFavorites.TYPE_DEPARTMENT);
        if (exist == null) {
            throw new BusinessErrorException("未关注该部门，无法取消");
        }

        // 4. 更新关注状态为取消
        exist.setFavoriteStatus(UserFavorites.STATUS_CANCELLED);
        baseMapper.updateById(exist);

        return exist;
    }

    // ------------------------------ 通用工具方法 ------------------------------
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        String token = getTokenFromRequest();
        return jwtUtil.getUserId(token);
    }

    /**
     * 获取当前登录用户角色
     */
    private String getCurrentUserRole() {
        String token = getTokenFromRequest();
        return jwtUtil.getRoleFromToken(token);
    }

    /**
     * 从请求头获取Token
     */
    private String getTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessErrorException("无法获取请求上下文");
        }
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }

    /**
     * 检查是否已收藏/关注
     */
    private boolean isAlreadyFavorited(Long userId, Long targetId, String targetType) {
        UserFavorites exist = baseMapper.selectByUserIdTargetIdAndType(
                userId,
                targetId,
                targetType,
                UserFavorites.STATUS_FAVORITED
        );
        return exist != null;
    }

    /**
     * 获取已存在的收藏/关注记录
     */
    private UserFavorites getExistingFavorite(Long userId, Long targetId, String targetType) {
        return baseMapper.selectByUserIdTargetIdAndType(
                userId,
                targetId,
                targetType,
                UserFavorites.STATUS_FAVORITED
        );
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

        // 构建：部门ID → 完整Department对象（而非仅名称）
        Map<Long, Department> deptMap;
        if (!deptIds.isEmpty()) {
            List<Department> depts = departmentMapper.selectBatchIds(deptIds);
            deptMap = depts.stream()
                    .collect(Collectors.toMap(
                            Department::getDepartmentId,
                            Function.identity(),
                            (k1, k2) -> k1
                    ));
        } else {
            deptMap = new HashMap<>();
        }

        // 转换为ActivityDetailDTO（同时设置部门名称和部门对象）
        return activities.stream()
                .map(activity -> {
                    ActivityDetailDTO dto = new ActivityDetailDTO();
                    BeanUtils.copyProperties(activity, dto);

                    // 设置部门名称（兜底）
                    Department dept = deptMap.get(activity.getDepartmentId());
                    dto.setDepartmentName(dept != null ? dept.getDepartmentName() : "未知部门");
                    // 设置部门对象（解决department: null问题）
                    dto.setDepartment(dept != null ? dept : new Department());

                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Override
    public int countFavoriteDepartments(Long userId) {
        if (userService.getById(userId) == null) {
            throw new BusinessErrorException("用户不存在");
        }
        return baseMapper.countByUserIdAndTargetType(
                userId,
                UserFavorites.TYPE_DEPARTMENT,
                UserFavorites.STATUS_FAVORITED
        );
    }

    @Override
    public int countFavoriteActivities(Long userId) {
        if (userService.getById(userId) == null) {
            throw new BusinessErrorException("用户不存在");
        }
        return baseMapper.countByUserIdAndTargetType(
                userId,
                UserFavorites.TYPE_ACTIVITY,
                UserFavorites.STATUS_FAVORITED
        );
    }

    @Override
    public int countDepartmentFollowers(Long departmentId) {
        // 验证部门是否存在
        if (departmentService.getById(departmentId) == null) {
            throw new BusinessErrorException("部门不存在");
        }
        // 统计状态为"已收藏"的用户数（去重）
        return baseMapper.countByTargetIdAndType(
                departmentId,
                UserFavorites.TYPE_DEPARTMENT,
                UserFavorites.STATUS_FAVORITED
        );
    }
    // 新增：实现根据部门ID查询收藏该部门的用户ID列表
    @Override
    public List<Long> getUserIdsByFavoriteDepartment(Long departmentId) {
        // 1. 校验部门是否存在
        if (departmentService.getById(departmentId) == null) {
            throw new BusinessErrorException("部门不存在");
        }

        // 2. 查询所有收藏该部门且状态为"已收藏"的记录
        LambdaQueryWrapper<UserFavorites> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFavorites::getTargetId, departmentId)
                .eq(UserFavorites::getTargetType, UserFavorites.TYPE_DEPARTMENT) // 收藏类型为部门
                .eq(UserFavorites::getFavoriteStatus, UserFavorites.STATUS_FAVORITED) // 已收藏状态
                .eq(UserFavorites::getIsDeleted, 0); // 未删除

        List<UserFavorites> favorites = baseMapper.selectList(queryWrapper);

        // 3. 提取用户ID列表
        return favorites.stream()
                .map(UserFavorites::getUserId)
                .distinct() // 去重，避免重复通知同一用户
                .collect(Collectors.toList());
    }
    @Override
    public List<UserFavorites> getUserAllFavorites(Long userId) {
        if (userService.getById(userId) == null) {
            throw new BusinessErrorException("用户不存在");
        }
        // 查询用户所有类型的收藏记录
        return baseMapper.selectByUserIdAndType(userId, null);
    }
}
