package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Admin;
import com.example.springboot.service.ActivityService;
import com.example.springboot.service.DepartmentService;
import com.example.springboot.dto.DepartmentDetailDTO;
import com.example.springboot.dto.DepartmentUpdateDTO;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.Department;
import com.example.springboot.excption.BusinessErrorException;
import com.example.springboot.mapper.DepartmentMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service // 仅标注一次，避免重复Bean
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Value("${department.avatar.path:D:/campus/admin_avatar/}")
    private String departmentAvatarPath;

    @Autowired
    private ActivityService activityService; // 注入活动服务，查询部门举办的活动

    // 根据部门ID查询部门
    @Override
    public Department getByDeptId(Long departmentId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<Department>()
                .eq(Department::getDepartmentId, departmentId));
    }

    // 部门头像上传
    @Override
    public String uploadDeptAvatar(Long departmentId, MultipartFile file) {
        // 校验文件非空
        if (file.isEmpty()) {
            throw new BusinessErrorException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessErrorException("上传文件无有效名称");
        }

        // 校验文件类型（仅支持jpg/png）
        boolean isJpg = originalFilename.endsWith(".jpg") || originalFilename.endsWith(".jpeg");
        boolean isPng = originalFilename.endsWith(".png");
        if (!isJpg && !isPng) {
            throw new BusinessErrorException("仅支持jpg/png格式文件");
        }

        // 生成唯一文件名（避免覆盖）
        String suffix = getFileSuffix(originalFilename);
        String fileName = "department_" + departmentId + "_" + System.currentTimeMillis() + suffix;
        File destFile = new File(departmentAvatarPath + fileName);

        try {
            // 确保存储目录存在
            File parentDir = destFile.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new BusinessErrorException("头像存储目录创建失败");
            }
            // 保存文件并更新数据库头像URL
            FileUtils.copyInputStreamToFile(file.getInputStream(), destFile);
            String avatarUrl = "/admin_avatar/" + fileName;

            Department department = new Department();
            department.setDepartmentId(departmentId);
            department.setAvatar(avatarUrl);
            updateById(department); // 更新头像字段

            return avatarUrl;
        } catch (IOException e) {
            throw new BusinessErrorException("头像上传失败：" + e.getMessage());
        }
    }


    // 查看部门详情（含该部门举办的活动）
    @Override
    public DepartmentDetailDTO getDepartmentDetail(Long departmentId) {
        // 1. 查询部门基本信息
        Department department = getByDeptId(departmentId);
        if (department == null) {
            throw new BusinessErrorException("部门不存在");
        }

        // 2. 查询部门举办的活动（假设Activity表有department_id字段）
        List<Activity> activities = activityService.list(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getDepartmentId, departmentId));

        activities.forEach(activity -> activity.setDepartment(department));

        // 3. 组装DTO返回
        DepartmentDetailDTO detailDTO = new DepartmentDetailDTO();
        detailDTO.setDepartment(department);
        detailDTO.setActivities(activities);
        return detailDTO;
    }

    // 编辑部门信息（名称和描述）
    @Override
    public boolean updateDepartment(DepartmentUpdateDTO updateDTO) {
        // 1. 校验部门是否存在
        Department department = getByDeptId(updateDTO.getDepartmentId());
        if (department == null) {
            throw new BusinessErrorException("部门不存在");
        }

        // 2. 更新名称和描述
        department.setDepartmentName(updateDTO.getDepartmentName());
        department.setDescription(updateDTO.getDescription());
        return baseMapper.updateById(department) > 0;
    }

    private String getFileSuffix(String originalFilename) {
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }
//查看该部门发布的活动列表
    @Override
    public List<Activity> getDepartmentActivities(Long departmentId) {
        // 1. 校验部门是否存在
        Department department = getByDeptId(departmentId);
        if (department == null) {
            throw new BusinessErrorException("部门不存在");
        }

        // 2. 查询该部门举办的所有活动
        return activityService.list(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getDepartmentId, departmentId)
                .orderByDesc(Activity::getCreatedTime)); // 按创建时间倒序，最新的在前
    }
}