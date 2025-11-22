package com.example.springboot.Service;

import com.example.springboot.dto.AdminDetailDTO;
import com.example.springboot.entity.Admin;
import org.springframework.web.multipart.MultipartFile;

public interface AdminService {
    // 根据工号查询管理员（必须定义，否则LoginServiceImpl会报错）
    Admin getByWorkId(String workId);
    // 新增：编辑管理员资料（仅密码）
    boolean updateAdminInfo(Long adminId, Admin updateAdmin);
    // 新增：上传管理员头像
    String uploadAvatar(Long adminId, MultipartFile file);
    Admin getById(Long adminId);
    AdminDetailDTO getAdminDetail(Long adminId);
}
