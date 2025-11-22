package com.example.springboot.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.dto.AdminDetailDTO;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.Department;
import com.example.springboot.excption.BusinessErrorException;
import com.example.springboot.mapper.AdminMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final DepartmentService departmentService; // 待注入的部门服务
    private final ActivityService activityService;     // 待注入的活动服务
    // 定义管理员头像存储路径常量
    private static final String ADMIN_AVATAR_PATH = "D:/campus/admin_avatar/"; // 路径根据实际情况调整
    // 构造器注入所有依赖（关键：通过参数接收并赋值）
    @Autowired // 显式添加@Autowired，确保Spring注入（多构造器时必须加）
    public AdminServiceImpl(BCryptPasswordEncoder passwordEncoder,
                            DepartmentService departmentService,
                            ActivityService activityService) {
        this.passwordEncoder = passwordEncoder;
        this.departmentService = departmentService; // 赋值给字段
        this.activityService = activityService;     // 赋值给字段
    }

    // 手动实现getById：通过adminId查询管理员
    @Override
    public Admin getById(Long adminId) {
        // 直接调用Mapper的selectById方法（AdminMapper必须继承BaseMapper）
        return baseMapper.selectById(adminId);
    }

    @Override
    public Admin getByWorkId(String workId) {
        return baseMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getWorkId, workId)
        );
    }

    // 编辑管理员资料（仅允许修改密码）
    @Override
    public boolean updateAdminInfo(Long adminId, Admin updateAdmin) {
        Admin admin = baseMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessErrorException("管理员不存在");
        }
        // 仅允许修改密码（其他字段忽略）
        if (updateAdmin.getPassword() != null && !updateAdmin.getPassword().trim().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(updateAdmin.getPassword()));
        }
        return baseMapper.updateById(admin) > 0;
    }

    // 上传管理员头像
    @Override
    public String uploadAvatar(Long adminId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessErrorException("上传文件无有效名称");
        }
        // 校验文件类型
        boolean isJpg = originalFilename.endsWith(".jpg") || originalFilename.endsWith(".jpeg");
        boolean isPng = originalFilename.endsWith(".png");
        if (!isJpg && !isPng) {
            throw new BusinessErrorException("仅支持jpg/png格式");
        }
        // 生成唯一文件名（区分用户头像）
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = "admin_" + adminId + "_" + System.currentTimeMillis() + suffix;
        File dest = new File(ADMIN_AVATAR_PATH + fileName);
        try {
            // 确保目录存在
            File parentDir = dest.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new BusinessErrorException("管理员头像目录创建失败");
            }
            // 保存文件
            FileUtils.copyInputStreamToFile(file.getInputStream(), dest);
            // 更新头像URL
            String avatarUrl = "/admin_avatar/" + fileName;
            Admin admin = new Admin();
            admin.setId(adminId);
            admin.setAvatar(avatarUrl);
            baseMapper.updateById(admin);
            return avatarUrl;
        } catch (IOException e) {
            throw new BusinessErrorException("管理员头像上传失败：" + e.getMessage());
        }
    }

    // 新增：查询管理员详情（含部门和活动）—— 现在可正常调用服务
    @Override
    public AdminDetailDTO getAdminDetail(Long adminId) {
        Admin admin = baseMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessErrorException("管理员不存在");
        }
        admin.setPassword(null);

        // 现在可正常调用注入的服务
        Department department = departmentService.getByDeptId(admin.getDepartment_id());
        List<Activity> activities = activityService.getByDepartmentId(admin.getDepartment_id());

        AdminDetailDTO detailDTO = new AdminDetailDTO();
        detailDTO.setAdmin(admin);
        detailDTO.setDepartment(department);
        detailDTO.setActivities(activities);
        return detailDTO;
    }
}