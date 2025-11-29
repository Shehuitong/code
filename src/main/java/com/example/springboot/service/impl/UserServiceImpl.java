package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.dto.UserProfileDTO;
import com.example.springboot.dto.UserRegisterDTO;
import com.example.springboot.entity.User;
import com.example.springboot.excption.BusinessErrorException;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 构造器注入（替代字段注入）
    private final BCryptPasswordEncoder passwordEncoder;
    // 头像存储路径（实际项目建议用OSS，避免本地路径依赖）
    private static final String AVATAR_PATH = "D:/campus/avatar/";
    private static final String DEFAULT_SUFFIX = ".png";
    @Autowired
    public UserServiceImpl(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // 1. 实现接口：根据学号查询用户
    @Override
    public User getByStudentId(String studentId) {
        return baseMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getStudentId, studentId)
        );
    }

    // 2.查看个人资料
    @Override
        public UserProfileDTO getUserInfo(Long userId) {

            User user = baseMapper.selectById(userId);
            if (user == null) {
                throw new BusinessErrorException("用户不存在");
            }
            UserProfileDTO profileDTO = new UserProfileDTO();
            BeanUtils.copyProperties(user, profileDTO);
            return profileDTO;
    }

    // 编辑资料（头像和手机号）
    @Override
    public boolean updateUserInfo(Long userId, String newPhone) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessErrorException("用户不存在");
        }

        // 仅更新传递的非空字段（不传递则保留原有值，避免覆盖）

        // 手机号处理：如果传递了新手机号，校验格式并更新；未传递则不处理
        if (newPhone != null) {
            if (!newPhone.matches("^1[3-9]\\d{9}$")) {
                throw new BusinessErrorException("手机号格式不正确，需输入11位有效手机号");
            }
            user.setPhone(newPhone); // 新增：将新手机号设置到用户实体
        }

        return baseMapper.updateById(user) > 0;
    }

    // 实现密码修改
    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessErrorException("用户不存在");
        }
        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessErrorException("原密码不正确");
        }
        // 新密码格式已通过 DTO 注解校验，此处可省略重复校验
        user.setPassword(passwordEncoder.encode(newPassword));
        return baseMapper.updateById(user) > 0;
    }
//4.头像上传
    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 1. 核心校验（文件非空+用户存在，合并关键校验）
        if (file.isEmpty() || file.getSize() == 0) {
            throw new RuntimeException("上传文件为空，请选择有效图片");
        }
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessErrorException("用户不存在，无法上传头像");
        }

        // 2. 简化文件名生成（优化后缀处理+路径拼接）
        String origName = file.getOriginalFilename();
        // 后缀处理：用Optional避免null判断，简洁优雅
        String suffix = Optional.ofNullable(origName)
                .filter(name -> name.contains("."))
                .map(name -> name.substring(name.lastIndexOf(".")))
                .orElse(DEFAULT_SUFFIX);
        String fileName = userId + "_" + System.currentTimeMillis() + suffix;
        File destFile = Paths.get(AVATAR_PATH, fileName).toFile(); // 规范路径拼接（避免字符串拼接错误）

        // 3. 简化目录创建（mkdirs失败直接抛异常，无需额外判断）
        File dir = destFile.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("头像存储目录创建失败，路径：" + dir.getAbsolutePath());
        }

        // 4. 文件复制+完整性校验（合并异常处理，减少冗余）
        try {
            FileUtils.copyInputStreamToFile(file.getInputStream(), destFile);
            // 校验文件完整性（核心逻辑保留）
            if (destFile.length() != file.getSize()) {
                throw new IOException("文件复制不完整，原大小：" + file.getSize() + "字节，目标大小：" + destFile.length() + "字节");
            }
            log.info("文件上传成功！大小：{}KB，路径：{}", file.getSize() / 1024, destFile);
        } catch (IOException e) {
            // 异常时删除无效文件（合并删除逻辑，避免重复）
            if (destFile.exists() && !destFile.delete()) {
                log.warn("无效文件删除失败，路径：{}", destFile);
            }
            log.error("头像上传失败", e);
            throw new RuntimeException("头像上传失败：" + e.getMessage());
        }

        // 5. 数据库更新（逻辑不变，保持简洁）
        String avatarUrl = "/avatar/" + fileName;
        user.setAvatarUrl(avatarUrl);
        baseMapper.updateById(user);
        log.info("用户[ID:{}]头像更新成功，URL：{}", userId, avatarUrl);

        return avatarUrl;
    }
    // 5. 实现接口：用户注册
    @Override
    public User register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        // 1. 用户名格式校验（4-20位字母/数字）
        String userName = registerDTO.getUserName();
        if (userName == null || !userName.matches("^[a-zA-Z\u4e00-\u9fa5]{2,20}$")) {
            throw new BusinessErrorException("用户名格式不正确，正确格式：2-20位");
        }

        // 2. 学生ID格式校验（假设为8位数字）
        String studentId = registerDTO.getStudentId();
        if (studentId == null || !studentId.matches("^202[2-5]\\d{3}\\d{2}$")) {
            throw new BusinessErrorException("学生ID格式不正确，正确格式：4位年级（2022-2025）+3位学院号+2位座位号（共9位）");
        }

        // 3. 密码格式校验（至少6位，包含字母和数字）
        String password = registerDTO.getPassword();
        if (password == null || !password.matches("^(?=.*[A-Za-z])(?=.*\\d).{6,}$")) {
            throw new BusinessErrorException("密码格式不正确，正确格式：至少6位，包含字母和数字");
        }

        // 4. 手机号格式校验（11位数字，可选，若填写则校验）
        String phone = registerDTO.getPhone();
        if (phone != null && !phone.trim().isEmpty() && !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessErrorException("手机号格式不正确，正确格式：11位数字（以13/14/15/17/18/19开头）");
        }

        // 校验用户是否已存在：仅当用户名和学号完全相同时，才视为同一用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 同时匹配用户名和学号
        queryWrapper.eq(User::getUsername, userName)
                .eq(User::getStudentId, registerDTO.getStudentId());
        if (baseMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessErrorException("用户已存在");
        }

        queryWrapper.clear();
        queryWrapper.eq(User::getStudentId, studentId);
        if (baseMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessErrorException("学生ID已被注册");
        }

        //  校验两次输入的密码是否一致
        String confirmPassword = registerDTO.getConfirmPassword();
        if (!password.equals(confirmPassword)) {
            throw new BusinessErrorException("两次输入的密码不一致，请重新输入");
        }

        // 构建用户实体并保存（原有逻辑）
        User user = new User();
        user.setUsername(userName);
        user.setStudentId(studentId);
        user.setCollege(registerDTO.getCollege());
        user.setPassword(passwordEncoder.encode(password));
        user.setGrade(registerDTO.getGrade());
        user.setPhone(phone);
        baseMapper.insert(user);
        return user;
    }
}