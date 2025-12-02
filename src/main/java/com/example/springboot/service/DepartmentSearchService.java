package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.dto.DepartmentDetailDTO;
import com.example.springboot.dto.DepartmentListDTO;
import com.example.springboot.dto.DepartmentSuggestDTO;
import com.example.springboot.entity.Department;

import java.util.List;

/**
 * 部门搜索服务接口
 */
public interface DepartmentSearchService extends IService<Department> {
    // 1. 搜索联想：根据关键词返回部门名称列表
    List<DepartmentSuggestDTO> getSearchSuggest(String keyword);

    // 2. 搜索结果分页：根据关键词返回部门列表
    List<DepartmentListDTO> searchDepartmentList(String keyword);

    // 3. 部门详情：根据部门ID返回完整信息
    DepartmentDetailDTO getDepartmentDetail(Long departmentId);
    // 根据部门ID查询部门信息
    Department getByDeptId(Long departmentId);
}