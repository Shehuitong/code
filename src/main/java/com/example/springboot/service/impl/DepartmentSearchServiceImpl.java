package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public class DepartmentSearchServiceImpl implements DepartmentSearchService {

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
    public Page<DepartmentListDTO> searchDepartmentList(String keyword, Integer pageNum, Integer pageSize) {
        // 参数校验：关键词为空抛异常
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("搜索关键词不能为空！");
        }
        // 分页参数默认值：页码1，每页10条
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;

        // 1. 构造查询条件：模糊匹配 + 按创建时间倒序
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.and(i -> i.like("department_name", keyword.trim())
                        .or().like("department_college", keyword.trim())
                        .or().like("description", keyword.trim()))
                .orderByDesc("create_time");

        // 2. 分页查询
        Page<Department> departmentPage = departmentMapper.selectPage(
                new Page<>(pageNum, pageSize),
                wrapper
        );

        // 3. 实体转DTO
        Page<DepartmentListDTO> listDTOPage = new Page<>();
        BeanUtils.copyProperties(departmentPage, listDTOPage);
        List<DepartmentListDTO> dtoList = departmentPage.getRecords().stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
        listDTOPage.setRecords(dtoList);

        return listDTOPage;
    }

    /**
     * 3. 部门详情：根据ID查询完整信息（含部门举办的活动）
     */
    @Override
    public DepartmentDetailDTO getDepartmentDetail(Long deptId) {
        // 参数校验：ID为空抛异常
        if (deptId == null) {
            throw new IllegalArgumentException("部门ID不能为空！");
        }

        // 1. 查询部门
        Department department = departmentMapper.selectById(deptId);
        if (department == null) {
            throw new BusinessErrorException("部门不存在！");
        }

        // 2. 查询部门举办的活动
        List<Activity> activities = activityService.getByDepartmentId(deptId);

        // 3. 组装DTO返回
        DepartmentDetailDTO detailDTO = new DepartmentDetailDTO();
        detailDTO.setDepartment(department);
        detailDTO.setActivities(activities);

        return detailDTO;
    }

    /**
     * 工具方法：Department → DepartmentListDTO
     */
    private DepartmentListDTO convertToListDTO(Department department) {
        DepartmentListDTO dto = new DepartmentListDTO();
        BeanUtils.copyProperties(department, dto);
        dto.setDepartmentId(department.getDeptId());
        return dto;
    }
}