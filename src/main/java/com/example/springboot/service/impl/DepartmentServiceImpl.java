package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service // 仅标注一次，避免重复Bean
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    // 头像存储路径（建议配置在application.yml，这里简化写死）
    private static final String DEPT_AVATAR_PATH = "D:/campus/dept_avatar/";
    private static final String ADMIN_AVATAR_PATH = "D:/campus/admin_avatar/";

    @Autowired
    private ActivityService activityService; // 注入活动服务，查询部门举办的活动

    // 根据部门ID查询部门
    @Override
    public Department getByDeptId(Long deptId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<Department>()
                .eq(Department::getDeptId, deptId));
    }

    // 部门头像上传
    @Override
    public String uploadDeptAvatar(Long deptId, MultipartFile file) {
        return uploadAvatar(deptId, file, DEPT_AVATAR_PATH, "dept_");
    }

    // 管理员头像上传（复用头像上传逻辑）
    public String uploadAdminAvatar(Long adminId, MultipartFile file) {
        return uploadAvatar(adminId, file, ADMIN_AVATAR_PATH, "admin_");
    }

    // 通用头像上传工具方法（提取重复逻辑）
    private <T> String uploadAvatar(T id, MultipartFile file, String basePath, String prefix) {
        // 1. 校验文件
        if (file.isEmpty()) {
            throw new BusinessErrorException("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".jpg") && !originalFilename.endsWith(".png"))) {
            throw new BusinessErrorException("仅支持JPG/PNG格式文件");
        }

        // 2. 生成文件名（避免重复）
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = prefix + id + "_" + System.currentTimeMillis() + suffix;
        File destFile = new File(basePath + fileName);

        // 3. 创建目录并保存文件
        try {
            File parentDir = destFile.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new BusinessErrorException("头像存储目录创建失败");
            }
            FileUtils.copyInputStreamToFile(file.getInputStream(), destFile);
        } catch (IOException e) {
            throw new BusinessErrorException("头像上传失败：" + e.getMessage());
        }

        // 4. 返回头像URL（前端可通过该URL访问）
        return "/" + prefix + "avatar/" + fileName; // 示例：/dept_avatar/dept_1_123456.png
    }

    // 查看部门详情（含该部门举办的活动）
    @Override
    public DepartmentDetailDTO getDepartmentDetail(Long deptId) {
        // 1. 查询部门基本信息
        Department department = getByDeptId(deptId);
        if (department == null) {
            throw new BusinessErrorException("部门不存在");
        }

        // 2. 查询部门举办的活动（假设Activity表有department_id字段）
        List<Activity> activities = activityService.list(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getDepartmentId, deptId));

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
        Department department = getByDeptId(updateDTO.getDeptId());
        if (department == null) {
            throw new BusinessErrorException("部门不存在");
        }

        // 2. 更新名称和描述
        department.setDepartmentName(updateDTO.getDepartmentName());
        department.setDescription(updateDTO.getDescription());
        return baseMapper.updateById(department) > 0;
    }
}