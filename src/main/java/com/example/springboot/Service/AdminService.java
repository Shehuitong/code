package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.dto.AdminPersonalInfoDTO;
import com.example.springboot.entity.Admin;
import org.springframework.web.multipart.MultipartFile;

public interface AdminService extends IService<Admin> {
    // 根据工号查询管理员
    Admin getByWorkId(String workId);

    // 获取管理员个人信息（昵称、工号、头像、部门名/ID）
    AdminPersonalInfoDTO getAdminPersonalInfo(Long adminId);

    // 独立修改密码接口
    boolean updateAdminPassword(Long adminId, String oldPassword, String newPassword);

    // 独立头像上传接口
    String uploadAvatar(Long adminId, MultipartFile file);
}