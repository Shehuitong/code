package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.dto.DepartmentDetailDTO;
import com.example.springboot.dto.DepartmentListDTO;
import com.example.springboot.dto.DepartmentSuggestDTO;
import com.example.springboot.service.DepartmentSearchService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门搜索控制器
 */
@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentSearchController {

    private final DepartmentSearchService departmentSearchService;

    /**
     * 1. 搜索联想接口（前端输入关键词时调用）
     * 请求示例：http://localhost:8080/api/department/search/suggest?keyword=计算机
     */
    @GetMapping("/search/suggest")
    public ResponseEntity<List<DepartmentSuggestDTO>> getSearchSuggest(
            @RequestParam String keyword
    ) {
        List<DepartmentSuggestDTO> suggestList = departmentSearchService.getSearchSuggest(keyword);
        return ResponseEntity.ok(suggestList);
    }

    /**
     * 2. 搜索结果分页接口（用户点击“搜索”时调用）
     * 请求示例：http://localhost:8080/api/department/search?keyword=计算机
     */
    @GetMapping("/search")
    public ResponseEntity<List<DepartmentListDTO>> searchDepartmentList(
            @RequestParam String keyword
    ) {
        List<DepartmentListDTO> departmentList = departmentSearchService.searchDepartmentList(keyword);
        return ResponseEntity.ok(departmentList);
    }

    /**
     * 3. 部门详情接口（用户点击“查看详情”时调用）
     * 请求示例：http://localhost:8080/api/department/detail/1
     */
    @GetMapping("/detail/{departmentId}")
    public ResponseEntity<DepartmentDetailDTO> getDepartmentDetail(
            @PathVariable Long departmentId
    ) {
        DepartmentDetailDTO detailDTO = departmentSearchService.getDepartmentDetail(departmentId);
        return ResponseEntity.ok(detailDTO);
    }
    // 新增：获取所有部门详情
    @GetMapping("/all/detail")
    public ResponseEntity<List<DepartmentDetailDTO>> getAllDepartmentsDetail() {
        List<DepartmentDetailDTO> allDetails = departmentSearchService.getAllDepartmentsDetail();
        return ResponseEntity.ok(allDetails);
    }
}