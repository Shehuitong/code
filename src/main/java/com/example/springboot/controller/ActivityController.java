package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Activity;
import com.example.springboot.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动模块控制器
 * 对接路径：/api/activity
 */
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /**
     * 根据活动ID查询详情（支持获取剩余名额、已报名人数）
     * 路径：/api/activity/{id}
     */
    @GetMapping("/{id}")
    public Result<Activity> getActivityById(@PathVariable Long id) {
        Activity activity = activityService.getById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        return Result.success(activity);
    }

    /**
     * 查询所有活动（支持分页，这里先实现基础列表）
     * 路径：/api/activity/list
     */
    @GetMapping("/list")
    public Result<List<Activity>> getActivityList() {
        List<Activity> activityList = activityService.list();
        return Result.success(activityList);
    }

    /**
     * 根据部门ID查询活动
     * 路径：/api/activity/department/{deptId}
     */
    @GetMapping("/department/{deptId}")
    public Result<List<Activity>> getActivityByDeptId(@PathVariable Integer deptId) {
        List<Activity> activityList = activityService.lambdaQuery()
                .eq(Activity::getDepartmentId, deptId)
                .list();
        return Result.success(activityList);
    }

    /**
     * 查询活动剩余名额（单独接口，可选）
     * 路径：/api/activity/{id}/remaining
     */
    @GetMapping("/{id}/remaining")
    public Result<Integer> getActivityRemaining(@PathVariable Long id) {
        Activity activity = activityService.getById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        return Result.success(activity.getRemainingPeople());
    }

    /**
     * 查询活动已报名人数（单独接口，可选）
     * 路径：/api/activity/{id}/applyCount
     */
    @GetMapping("/{id}/applyCount")
    public Result<Integer> getActivityApplyCount(@PathVariable Long id) {
        Activity activity = activityService.getById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        return Result.success(activity.getApplyCount());
    }
}