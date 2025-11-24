package com.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.dto.ActivityDetailDTO;
import com.example.springboot.dto.ActivityListDTO;
import com.example.springboot.dto.ActivitySuggestDTO;
import com.example.springboot.Service.ActivitySearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动搜索控制器（暴露接口给前端）
 */
@RestController
@RequestMapping("/api/activity") // 接口统一前缀
@RequiredArgsConstructor
public class ActivitySearchController {

    private final ActivitySearchService activitySearchService;

    /**
     * 1. 搜索联想接口（前端输入关键词时调用）
     * 请求示例：http://localhost:8080/api/activity/search/suggest?keyword=竞赛
     */
    @GetMapping("/search/suggest")
    public ResponseEntity<List<ActivitySuggestDTO>> getSearchSuggest(
            @RequestParam String keyword // 搜索关键词
    ) {
        List<ActivitySuggestDTO> suggestList = activitySearchService.getSearchSuggest(keyword);
        return ResponseEntity.ok(suggestList); // 200 OK + 联想列表
    }

    /**
     * 2. 搜索结果分页接口（用户点击“搜索”时调用）
     * 请求示例：http://localhost:8080/api/activity/search?keyword=竞赛&pageNum=1&pageSize=10
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ActivityListDTO>> searchActivityList(
            @RequestParam String keyword, // 必传：搜索关键词
            @RequestParam(required = false) Integer pageNum, // 可选：页码
            @RequestParam(required = false) Integer pageSize // 可选：每页条数
    ) {
        Page<ActivityListDTO> activityPage = activitySearchService.searchActivityList(keyword, pageNum, pageSize);
        return ResponseEntity.ok(activityPage); // 200 OK + 分页结果
    }

    /**
     * 3. 活动详情接口（用户点击“查看详情”时调用）
     * 请求示例：http://localhost:8080/api/activity/detail/1
     */
    @GetMapping("/detail/{activityId}")
    public ResponseEntity<ActivityDetailDTO> getActivityDetail(
            @PathVariable Long activityId // 路径参数：活动ID
    ) {
        ActivityDetailDTO detailDTO = activitySearchService.getActivityDetail(activityId);
        return ResponseEntity.ok(detailDTO); // 200 OK + 详情信息
    }
}