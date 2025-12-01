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
import com.example.springboot.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.role;

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
                                    DepartmentService departmentService,
                                    ActivityService activityService,
                                    DepartmentMapper departmentMapper,
                                    JwtUtil jwtUtil) {
        this.userService = userService;
        this.departmentService = departmentService;
        this.activityService = activityService;
        this.departmentMapper = departmentMapper;
        this.jwtUtil = jwtUtil;
    }
    @Override
    public void addFavorite(Long targetId, String targetType) {
        // 1. 从请求头获取token（与活动报名逻辑一致）
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessErrorException("无法获取请求上下文");
        }
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // 移除Bearer前缀
        }

        // 2. 用JwtUtil解析用户ID和角色（核心修改：适配JwtUtil的参数要求）
        Long userId = jwtUtil.getUserId(token);  // 传入token参数
        String role = jwtUtil.getRoleFromToken(token);  // 传入token参数，获取角色

        // 3. 校验用户是否存在
        if (userService.getById(userId) == null) {
            throw new BusinessErrorException("用户不存在");
        }

        // 4. 权限控制：管理员（role为"admin"）不能收藏活动
        if (UserFavorites.TYPE_ACTIVITY.equals(targetType) && "admin".equals(role)) {  // 注意角色值与JwtUtil中一致（小写"admin"）
            throw new BusinessErrorException("管理员不能收藏活动");
        }

        // 5. 校验目标（活动/部门）是否存在
        if (UserFavorites.TYPE_DEPARTMENT.equals(targetType)) {
            if (departmentService.getById(targetId) == null) {
                throw new BusinessErrorException("部门不存在");
            }
        } else if (UserFavorites.TYPE_ACTIVITY.equals(targetType)) {
            if (activityService.getById(targetId) == null) {
                throw new BusinessErrorException("活动不存在");
            }
        } else {
            throw new BusinessErrorException("收藏类型错误（仅支持ACTIVITY/DEPARTMENT）");
        }

        // 6. 校验是否已收藏（仅查询“已收藏”状态的记录）
        UserFavorites exist = baseMapper.selectByUserIdTargetIdAndType(
                userId,
                targetId,
                targetType,
                UserFavorites.STATUS_FAVORITED  // 传入“已收藏”状态作为查询条件
        );
        if (exist != null) {
            throw new BusinessErrorException("已收藏，无需重复操作");
        }

        // 7. 保存收藏记录
        UserFavorites favorites = new UserFavorites();
        favorites.setUserId(userId);
        favorites.setTargetId(targetId);
        favorites.setTargetType(targetType);  // 类型为字符串（ACTIVITY/DEPARTMENT）
        baseMapper.insert(favorites);
    }
    @Override
    public void cancelFavorite(Long targetId, String targetType) {
        // 1. 从请求头获取token（与收藏逻辑一致）
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessErrorException("无法获取请求上下文");
        }
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 2. 用JwtUtil解析用户ID
        Long userId = jwtUtil.getUserId(token);  // 传入token参数

        // 3. 校验用户是否存在
        if (userService.getById(userId) == null) {
            throw new BusinessErrorException("用户不存在");
        }
        // 4. 权限控制：管理员（role为"admin"）不能收藏活动
        if (UserFavorites.TYPE_ACTIVITY.equals(targetType) && "admin".equals(role)) {  // 注意角色值与JwtUtil中一致（小写"admin"）
            throw new BusinessErrorException("管理员不能收藏部门");
        }
        // 4. 校验收藏记录是否存在
        UserFavorites exist = baseMapper.selectByUserIdTargetIdAndType(
                userId,
                targetId,
                targetType,
                UserFavorites.STATUS_FAVORITED  // 传入“已收藏”状态作为查询条件
        );
        if (exist == null) {
            throw new BusinessErrorException("未收藏，无法取消");
        }

        // 5. 删除收藏记录（逻辑删除或物理删除，根据你的表设计）
        // 修正后：更新状态为“已取消”
        exist.setFavoriteStatus(UserFavorites.STATUS_CANCELLED);
        baseMapper.updateById(exist);
        // 若使用物理删除：
        // baseMapper.deleteByUserIdTargetIdAndType(userId, targetId, targetType);
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
                                Department::getDepartmentId,
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
