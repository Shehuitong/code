package com.example.springboot.Service;
import com.example.springboot.dto.UserProfileDTO;
import com.example.springboot.entity.User;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.dto.UserRegisterDTO;
public interface UserService extends IService<User>{
    // 查看个人资料
    UserProfileDTO getUserInfo(Long userId);
    boolean updateUserInfo(Long userId, String newPhone, String newAvatarUrl);
    // 上传头像
    String uploadAvatar(Long userId, MultipartFile file);
    User register(UserRegisterDTO registerDTO);
    // 根据学号查询用户（必须定义）
    User getByStudentId(String studentId);
    // 修改密码
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
}
