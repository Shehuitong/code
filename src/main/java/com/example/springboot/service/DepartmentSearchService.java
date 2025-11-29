package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.dto.DepartmentDetailDTO;
import com.example.springboot.dto.DepartmentListDTO;
import com.example.springboot.dto.DepartmentSuggestDTO;

import java.util.List;

/**
 * 部门搜索服务接口
 */
public interface DepartmentSearchService {
    // 1. 搜索联想：根据关键词返回部门名称列表
    List<DepartmentSuggestDTO> getSearchSuggest(String keyword);

    // 2. 搜索结果分页：根据关键词返回部门列表（带分页）
    Page<DepartmentListDTO> searchDepartmentList(String keyword, Integer pageNum, Integer pageSize);

    // 3. 部门详情：根据部门ID返回完整信息
    DepartmentDetailDTO getDepartmentDetail(Long departmentId);
}