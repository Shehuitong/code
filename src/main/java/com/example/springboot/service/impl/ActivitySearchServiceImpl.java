package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.dto.ActivityDetailDTO;
import com.example.springboot.dto.ActivityListDTO;
import com.example.springboot.dto.ActivitySuggestDTO;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.Department;
import com.example.springboot.enums.ActivityStatusEnum;
import com.example.springboot.mapper.ActivityMapper;
import com.example.springboot.mapper.DepartmentMapper;
import com.example.springboot.service.ActivitySearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动搜索服务实现（核心业务逻辑）
 */
@Service
@RequiredArgsConstructor // Lombok自动注入Mapper（替代@Autowired）
public class ActivitySearchServiceImpl implements ActivitySearchService {

    // 注入活动Mapper和部门Mapper（详情页需部门名称）
    private final ActivityMapper activityMapper;
    private final DepartmentMapper departmentMapper;

    /**
     * 1. 搜索联想：返回活动名称包含关键词的标题（最多10个）
     */
    @Override
    public List<ActivitySuggestDTO> getSearchSuggest(String keyword) {
        // 参数校验：关键词为空返回空列表
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        String trimKeyword = keyword.trim();

        // 构造查询条件：仅匹配活动名称包含关键词 + 排除下架活动
        QueryWrapper<Activity> wrapper = new QueryWrapper<>();
        wrapper.like("activity_name", "%" + trimKeyword + "%") // 精确模糊匹配名称
                .ne("status", ActivityStatusEnum.OFFLINE) // 排除下架
                .select("activity_name") // 只查标题字段
                .orderByAsc("activity_name") // 按名称升序
                .last("limit 10"); // 最多10个

        // 查询并转DTO
        return activityMapper.selectList(wrapper).stream()
                .map(activity -> {
                    ActivitySuggestDTO dto = new ActivitySuggestDTO();
                    dto.setActivityName(activity.getActivityName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 2. 搜索结果分页：返回活动名称包含关键词的活动列表（带分页）
     */
    @Override
    public List<ActivityListDTO> searchActivityList(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("搜索关键词不能为空！");
        }

        String trimKeyword = keyword.trim();

        // 2. 全量查询（核心修改：移除分页，调用新的全量查询Mapper方法）
        List<Activity> activityList = activityMapper.selectAllActivityWithDeptByKeyword(
                trimKeyword,
                ActivityStatusEnum.OFFLINE // 排除下架活动
        );

        return activityList.stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    /**
     * 3. 活动详情：根据ID查询完整信息（含部门名称）
     */
    @Override
    public ActivityDetailDTO getActivityDetail(Long activityId) {
        if (activityId == null) {
            throw new IllegalArgumentException("活动ID不能为空！");
        }

        // 关联查询活动+部门完整信息（修改mapper方法，支持关联查询）
        Activity activity = activityMapper.selectActivityWithDeptById(activityId);
        if (activity == null || ActivityStatusEnum.OFFLINE.equals(activity.getStatus())) {
            throw new RuntimeException("活动不存在或已下架！");
        }

        ActivityDetailDTO detailDTO = new ActivityDetailDTO();
        BeanUtils.copyProperties(activity, detailDTO);  // 复制活动所有属性

        // 设置完整部门信息
        if (activity.getDepartment() != null) {
            detailDTO.setDepartment(activity.getDepartment());
            detailDTO.setDepartmentName(activity.getDepartment().getDepartmentName());
        } else {
            Department defaultDept = new Department();
            defaultDept.setDepartmentId(0L);
            defaultDept.setDepartmentName("未知部门");
            detailDTO.setDepartment(defaultDept);
        }

        return detailDTO;
    }

    @Override
    public List<ActivityDetailDTO> getAllActiveActivities() {
        // 查询所有未下架活动（关联部门信息），无分页
        List<Activity> activityList = activityMapper.selectAllActivityWithDept(
                ActivityStatusEnum.OFFLINE // 排除下架活动
        );

        // 转换为DTO并封装部门信息
        return activityList.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 工具方法：Activity → ActivityListDTO（确保复制报名时间等字段）
     */
    // ActivitySearchServiceImpl.java
    private ActivityListDTO convertToListDTO(Activity activity) {
        ActivityListDTO dto = new ActivityListDTO();
        BeanUtils.copyProperties(activity, dto);

        // 如果关联到部门，则直接使用；否则设为未知
        if (activity.getDepartment() != null) {
            dto.setDepartment(activity.getDepartment());
        } else {
            Department defaultDept = new Department();
            defaultDept.setDepartmentId(activity.getDepartmentId()); // 保留原始部门ID
            defaultDept.setDepartmentName("未知部门");
            dto.setDepartment(defaultDept);
        }
        return dto;
    }
    private ActivityDetailDTO convertToDetailDTO(Activity activity) {
        ActivityDetailDTO detailDTO = new ActivityDetailDTO();
        BeanUtils.copyProperties(activity, detailDTO);

        // 设置部门信息（兼容无关联部门的场景）
        if (activity.getDepartment() != null) {
            detailDTO.setDepartment(activity.getDepartment());
            detailDTO.setDepartmentName(activity.getDepartment().getDepartmentName()); // 关键步骤
        } else {
            Department defaultDept = new Department();
            defaultDept.setDepartmentId(0L);
            defaultDept.setDepartmentName("未知部门");
            detailDTO.setDepartment(defaultDept);
            detailDTO.setDepartmentName("未知部门");
        }
        return detailDTO;
    }
}