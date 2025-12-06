package com.example.springboot.controller;
import com.example.springboot.common.Result;
import com.example.springboot.dto.ActivityRegistrationExcelDTO;
import com.example.springboot.service.ActivityRegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/admin/activity")
@RequiredArgsConstructor
public class ActivityAdminController {

    private final ActivityRegistrationService activityRegistrationService;
    private final ObjectMapper objectMapper; // Spring默认已注入，用于将Result转JSON

    /**
     * 导出活动已报名用户信息（Excel格式）
     * 请求示例：http://localhost:8080/api/admin/activity/export/1
     * 正常情况：返回Excel文件流；异常情况：返回JSON格式错误信息
     */
    @GetMapping("/export/{activityId}")
    public void exportRegisteredUsers( // 修复1：返回void，不返回任何响应体
                                       @PathVariable Long activityId,
                                       HttpServletResponse response
    ) {
        try {
            // 调用服务层导出逻辑（Service已设置Excel响应头+写流）
            activityRegistrationService.exportRegisteredUsers(activityId, response);
        } catch (IllegalArgumentException e) {
            // 参数错误：返回400 + JSON格式Result
            handleException(response, 400, e.getMessage());
        } catch (RuntimeException e) {
            // 业务异常：返回500 + JSON格式Result
            handleException(response, 500, e.getMessage());
        } catch (IOException e) {
            // IO异常：返回500 + JSON格式Result
            handleException(response, 500, "文件导出失败：" + e.getMessage());
        }
    }
    /**
     * 查看活动已报名用户信息（与导出Excel内容一致）
     * 请求示例：http://localhost:8080/api/admin/activity/registrations/1
     */
    @GetMapping("/registrations/{activityId}")
    public Result<List<ActivityRegistrationExcelDTO>> getRegisteredUsers(
            @PathVariable Long activityId
    ) {
        try {
            // 调用服务层获取报名用户信息列表
            List<ActivityRegistrationExcelDTO> userList = activityRegistrationService.getRegisteredUsers(activityId);
            return Result.success(userList);
        } catch (IllegalArgumentException e) {
            // 移除状态码参数，仅传入错误信息
            return Result.error("参数错误：" + e.getMessage());
        } catch (RuntimeException e) {
            // 移除状态码参数，仅传入错误信息
            return Result.error("系统错误：" + e.getMessage());
        }
    }
    /**
     * 统一异常处理：往响应流写JSON格式的Result（避免ResponseEntity冲突）
     * @param response 响应对象
     * @param status 响应状态码（400/500）
     * @param message 错误信息
     */
    private void handleException(HttpServletResponse response, int status, String message) {
        try {
            // 1. 重置响应头为JSON格式（清除Excel相关头）
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(status); // 设置状态码（400/500）
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");

            // 2. 将Result对象转为JSON写入响应流
            Result errorResult = Result.error(message);
            try (OutputStream os = response.getOutputStream()) {
                objectMapper.writeValue(os, errorResult);
                os.flush(); // 确保数据写入流
            }
        } catch (IOException e) {
            // 异常处理本身失败，记录日志
            e.printStackTrace();
        }
    }
}