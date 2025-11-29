package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    public Page<ActivityListDTO> searchActivityList(String keyword, Integer pageNum, Integer pageSize) {
        // 参数校验：关键词为空抛异常
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("搜索关键词不能为空！");
        }
        // 分页参数默认值：页码1，每页10条
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
        String trimKeyword = keyword.trim();

        // 1. 构造查询条件：仅匹配活动名称包含关键词 + 排除下架 + 按发布时间倒序
        QueryWrapper<Activity> wrapper = new QueryWrapper<>();
        wrapper.like("activity_name", "%" + trimKeyword + "%") // 仅匹配活动名称
                .ne("status", ActivityStatusEnum.OFFLINE)
                .orderByDesc("created_time"); // 最新活动优先

        // 2. 分页查询：Page(pageNum, pageSize) → 页码从1开始
        Page<Activity> activityPage = activityMapper.selectPage(
                new Page<>(pageNum, pageSize),
                wrapper
        );

        // 3. 实体转DTO（Page对象转换）
        Page<ActivityListDTO> listDTOPage = new Page<>();
        // 复制分页元信息（总条数、总页数等）
        BeanUtils.copyProperties(activityPage, listDTOPage);
        // 转换活动列表数据（确保包含报名时间等信息）
        List<ActivityListDTO> dtoList = activityPage.getRecords().stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
        listDTOPage.setRecords(dtoList);

        return listDTOPage;
    }

    /**
     * 3. 活动详情：根据ID查询完整信息（含部门名称）
     */
    @Override
    public ActivityDetailDTO getActivityDetail(Long activityId) {
        // 参数校验：ID为空抛异常
        if (activityId == null) {
            throw new IllegalArgumentException("活动ID不能为空！");
        }

        // 1. 查询活动：不存在或已下架抛异常
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || ActivityStatusEnum.OFFLINE.equals(activity.getStatus())) {
            throw new RuntimeException("活动不存在或已下架！");
        }

        // 2. 关联查询部门名称（用于详情页显示“主办方”）
        Department department = departmentMapper.selectById(activity.getDepartmentId());
        String departmentName = department != null ? department.getDepartmentName() : "未知部门";

        // 3. 实体转DTO（包含全部活动信息）
        ActivityDetailDTO detailDTO = new ActivityDetailDTO();
        BeanUtils.copyProperties(activity, detailDTO);
        detailDTO.setDepartmentName(departmentName); // 补充部门名称

        return detailDTO;
    }

    /**
     * 工具方法：Activity → ActivityListDTO（确保复制报名时间等字段）
     */
    private ActivityListDTO convertToListDTO(Activity activity) {
        ActivityListDTO dto = new ActivityListDTO();
        BeanUtils.copyProperties(activity, dto);
        // 若有需要显式复制的字段（如报名时间），可在此补充
        // dto.setApplyTime(activity.getApplyTime());
        return dto;
    }
}