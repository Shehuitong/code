package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.dto.AdminPersonalInfoDTO;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.Department;
import com.example.springboot.excption.BusinessErrorException;
import com.example.springboot.mapper.AdminMapper;
import com.example.springboot.mapper.DepartmentMapper;
import com.example.springboot.service.AdminService;
import com.example.springboot.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService{

    // 仅保留必要依赖
    private final BCryptPasswordEncoder passwordEncoder;
    private final DepartmentService departmentService;
    private final AdminMapper adminMapper;
    private final DepartmentMapper departmentMapper;
    // 从配置文件注入头像路径（推荐配置在application.yml）
    @Value("${admin.avatar.path:D:/campus/admin_avatar/}")
    private String adminAvatarPath;



    // 构造器注入（无冗余依赖）
    @Autowired
    public AdminServiceImpl(BCryptPasswordEncoder passwordEncoder, DepartmentService departmentService,AdminMapper adminMapper, DepartmentMapper departmentMapper) {
        this.passwordEncoder = passwordEncoder;
        this.departmentService = departmentService;
        this.adminMapper = adminMapper;
        this.departmentMapper = departmentMapper;
    }

    // 根据工号查询管理员（登录/校验用）
    @Override
    public Admin getByWorkId(String workId) {
        return baseMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getWorkId, workId)
        );
    }

    // 管理员头像上传（独立接口，满足编辑资料核心需求）
    @Override
    public String uploadAvatar(Long adminId, MultipartFile file) {
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
        String fileName = "admin_" + adminId + "_" + System.currentTimeMillis() + suffix;
        File destFile = new File(adminAvatarPath + fileName);

        try {
            // 确保存储目录存在
            File parentDir = destFile.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new BusinessErrorException("头像存储目录创建失败");
            }
            // 保存文件并更新数据库头像URL
            FileUtils.copyInputStreamToFile(file.getInputStream(), destFile);
            String avatarUrl = "/admin_avatar/" + fileName;

            Admin admin = new Admin();
            admin.setId(adminId);
            admin.setAvatar(avatarUrl);
            updateById(admin); // 更新头像字段

            return avatarUrl;
        } catch (IOException e) {
            throw new BusinessErrorException("头像上传失败：" + e.getMessage());
        }
    }

    // 获取管理员个人信息（仅返回指定字段：昵称、工号、头像、部门名/ID）
    @Override
    public AdminPersonalInfoDTO getAdminPersonalInfo(Long adminId) {
        // 1. 查询管理员（用自带方法，确保拿到所有字段）
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }



        // 2. 查询部门（加 null 防护，避免传入 null 查不到）
        Department dept = null;
        if (admin.getDepartmentId() != null) { // 只有管理员的 department_id 不为 null，才查部门
            dept = departmentMapper.selectById(admin.getDepartmentId());
            log.info("通过 department_id：{} 查询到部门：{}", admin.getDepartmentId(), dept);
        }else {
            log.info("管理员 department_id 为 null，不查询部门信息，adminId: {}", adminId);
        }

        // 3. 组装 DTO（保持你的原有逻辑，只修正 null 处理）
        AdminPersonalInfoDTO dto = new AdminPersonalInfoDTO();
        dto.setAdminName(admin.getAdminName());
        dto.setWorkId(admin.getWorkId());
        dto.setAvatar(admin.getAvatar());

        // 部门信息赋值：优先用部门表数据，否则用管理员表的 department_id
        if (dept != null) {
            dto.setDepartmentId(dept.getDepartmentId()); // 部门表的 deptId（映射自 department_id，正确）
            dto.setDepartmentName(dept.getDepartmentName());
        } else {
            // 即使部门不存在，也返回管理员表的 department_id（不再是 null）
            dto.setDepartmentId(admin.getDepartmentId());
            dto.setDepartmentName("未知部门");
        }
        log.info("管理员个人信息查询完成，adminId: {}，返回DTO信息：{}", adminId, dto);
        return dto;
    }
    // 独立修改密码接口（与头像上传分离）
    @Override
    public boolean updateAdminPassword(Long adminId, String oldPassword, String newPassword) {
        Admin admin = getById(adminId);
        if (admin == null) {
            throw new BusinessErrorException("管理员不存在");
        }

        // 校验原密码（兼容数据库明文/加密密码）
        String dbPassword = admin.getPassword();
        boolean passwordMatch;
        if (dbPassword.startsWith("$2")) { // 已加密（BCrypt前缀）
            passwordMatch = passwordEncoder.matches(oldPassword, dbPassword);
        } else { // 明文（存量数据兼容）
            passwordMatch = oldPassword.equals(dbPassword);
        }

        if (!passwordMatch) {
            throw new BusinessErrorException("原密码错误");
        }

        // 加密新密码并更新
        admin.setPassword(passwordEncoder.encode(newPassword));
        return updateById(admin);
    }

    // 工具方法：提取文件后缀（减少重复代码）
    private String getFileSuffix(String originalFilename) {
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }
}