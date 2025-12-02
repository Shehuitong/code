package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.dto.DepartmentDetailDTO;
import com.example.springboot.dto.DepartmentListDTO;
import com.example.springboot.dto.DepartmentSuggestDTO;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.Department;
import com.example.springboot.excption.BusinessErrorException;
import com.example.springboot.mapper.DepartmentMapper;
import com.example.springboot.service.ActivityService;
import com.example.springboot.service.DepartmentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门搜索服务实现
 */
@Service
@RequiredArgsConstructor
public class DepartmentSearchServiceImpl extends ServiceImpl<DepartmentMapper, Department>  implements DepartmentSearchService {

    private final DepartmentMapper departmentMapper;
    private final ActivityService activityService;



    /**
     * 1. 搜索联想：返回含关键词的部门名称（最多10个）
     */
    @Override
    public List<DepartmentSuggestDTO> getSearchSuggest(String keyword) {
        // 参数校验：关键词为空返回空列表
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }

        // 构造查询条件：模糊匹配部门名称或所属学院
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.and(i -> i.like("department_name", keyword.trim())
                        .or().like("department_college", keyword.trim()))
                .select("department_id", "department_name", "department_college")
                .last("limit 10");

        // 查询并转DTO
        return departmentMapper.selectList(wrapper).stream()
                .map(department -> {
                    DepartmentSuggestDTO dto = new DepartmentSuggestDTO();
                    dto.setDepartmentName(department.getDepartmentName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 2. 搜索结果分页：返回匹配的部门列表（带分页）
     */
    @Override
    public List<DepartmentListDTO> searchDepartmentList(String keyword) {
        // 参数校验：关键词为空抛异常
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("搜索关键词不能为空！");
        }

        // 构造查询条件：模糊匹配 + 按创建时间倒序
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.and(i -> i.like("department_name", keyword.trim()))
                .orderByAsc("department_id");

        // 查询所有匹配的部门
        List<Department> departments = departmentMapper.selectList(wrapper);

        // 转换为DTO并加载活动信息（包括已下架）
        return departments.stream()
                .map(this::convertToListDTOWithActivities)
                .collect(Collectors.toList());
    }

    /**
     * 3. 部门详情：根据ID查询完整信息（含部门举办的活动）
     */
    @Override
    public DepartmentDetailDTO getDepartmentDetail(Long departmentId) {
        // 1. 查询部门基本信息
        Department department = getByDeptId(departmentId);
        if (department == null) {
            throw new BusinessErrorException("部门不存在");
        }

        // 2. 查询部门举办的活动（直接在当前方法写LambdaQueryWrapper，无需依赖ActivityService的自定义方法）
        List<Activity> activities = activityService.list(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getDepartmentId, departmentId) // 匹配部门ID
                .orderByDesc(Activity::getCreatedTime) // 保持原有的按创建时间倒序
        );

        activities.forEach(activity -> activity.setDepartment(department));

        // 3. 组装DTO返回
        DepartmentDetailDTO detailDTO = new DepartmentDetailDTO();
        detailDTO.setDepartment(department);
        detailDTO.setActivities(activities);
        return detailDTO;
    }
    @Override
    public Department getByDeptId(Long departmentId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<Department>()
                .eq(Department::getDepartmentId, departmentId));
    }
    /**
     * 工具方法：Department → DepartmentListDTO（包含所有活动）
     */
    private DepartmentListDTO convertToListDTOWithActivities(Department department) {
        DepartmentListDTO dto = new DepartmentListDTO();
        dto.setDepartment(department);

        // 查询该部门的所有活动（包括已下架）
        List<Activity> activities = activityService.getByDepartmentId(department.getDepartmentId());
        activities.forEach(activity -> activity.setDepartment(department));
        dto.setActivities(activities);


        return dto;
    }
}