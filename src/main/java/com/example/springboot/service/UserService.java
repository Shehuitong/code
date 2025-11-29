package com.example.springboot.service;
import com.example.springboot.dto.UserProfileDTO;
import com.example.springboot.entity.User;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.dto.UserRegisterDTO;
public interface UserService extends IService<User>{
    // 查看个人资料
    UserProfileDTO getUserInfo(Long userId);
    //编辑用户个人资料（手机号和头像）
    boolean updateUserInfo(Long userId, String newPhone);
    // 上传头像
    String uploadAvatar(Long userId, MultipartFile file);
    //用户注册
    User register(UserRegisterDTO registerDTO);
    // 根据学号查询用户（必须定义）
    User getByStudentId(String studentId);
    // 修改密码
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
}
