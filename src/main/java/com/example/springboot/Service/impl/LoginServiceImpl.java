package com.example.springboot.service.impl;

import com.example.springboot.dto.LoginDTO;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.User;
import com.example.springboot.excption.LoginException;
import com.example.springboot.service.AdminService;
import com.example.springboot.service.LoginService;
import com.example.springboot.service.UserService;
import com.example.springboot.vo.LoginResponse;
import com.example.springboot.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    private final AdminService adminService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtils;

    // 构造器注入（替代字段注入）
    @Autowired
    public LoginServiceImpl(AdminService adminService, UserService userService,
                            PasswordEncoder passwordEncoder, JwtUtil jwtUtils) {
        this.adminService = adminService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    // 账号格式正则
    private static final String USER_ACCOUNT_REGEX = "^20(22|23|24|25)\\d{5}$"; // 2022-2025+3学院+2座位=9位
    private static final String ADMIN_ACCOUNT_REGEX = "^G2025\\d{3}$"; // G2025+3位数字=8位
    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)[A-Za-z\\d]{8,12}$";

    @Override
    public LoginResponse login(LoginDTO loginDTO) {
        String account = loginDTO.getAccount();
        String inputPassword = loginDTO.getPassword(); // 登录时输入的明文密码
        log.info("尝试登录，账号：{}", account);

        // 1. 校验账号格式
        boolean isAdmin = account.matches(ADMIN_ACCOUNT_REGEX);
        boolean isUser = account.matches(USER_ACCOUNT_REGEX);
        if (!isAdmin && !isUser) {
            log.warn("账号格式错误，账号：{}", account);
            throw new LoginException("账号格式错误！用户账号：2022-2025+3位学院数+2位座位号；管理员账号：G2025+3位数字");
        }

        // 2. 校验密码格式
        if (!inputPassword.matches(PASSWORD_REGEX)) {
            log.warn("密码格式错误，账号：{}", account);
            throw new LoginException("密码格式错误！需包含大小写字母+数字，长度8-12位");
        }

        // 3. 管理员登录逻辑（适配明文/加密密码）
        if (isAdmin) {
            Admin admin = adminService.getByWorkId(account);
            if (admin == null) {
                throw new LoginException("管理员账号不存在");
            }
            String dbPassword = admin.getPassword(); // 数据库中的密码（明文或加密）

            // 校验密码：先判断数据库密码是否已加密
            if (isEncryptedPassword(dbPassword)) {
                // 已加密：用明文与加密密码比对
                if (!passwordEncoder.matches(inputPassword, dbPassword)) {
                    throw new LoginException("管理员密码错误");
                }
                log.info("管理员加密密码登录成功，账号：{}", account);
            } else {
                // 未加密（明文）：直接对比明文
                if (!inputPassword.equals(dbPassword)) {
                    throw new LoginException("管理员密码错误");
                }
                // 明文校验通过，自动加密并更新到数据库（仅更新一次）
                admin.setPassword(passwordEncoder.encode(inputPassword));
                adminService.updateById(admin);
                log.info("管理员明文密码登录成功，已自动加密密码，账号：{}", account);
            }
            return generateLoginResponse(admin.getId(), account, "admin");
        }

        // 4. 普通用户登录逻辑（与管理员逻辑一致）
        User user = userService.getByStudentId(account);
        if (user == null) {
            throw new LoginException("用户账号不存在");
        }
        String dbPassword = user.getPassword();

        if (isEncryptedPassword(dbPassword)) {
            // 已加密：明文比对加密密码
            if (!passwordEncoder.matches(inputPassword, dbPassword)) {
                throw new LoginException("用户密码错误");
            }
            log.info("用户加密密码登录成功，账号：{}", account);
        } else {
            // 未加密：明文对比，通过后加密更新
            if (!inputPassword.equals(dbPassword)) {
                throw new LoginException("用户密码错误");
            }
            user.setPassword(passwordEncoder.encode(inputPassword));
            userService.updateById(user);
            log.info("用户明文密码登录成功，已自动加密密码，账号：{}", account);
        }
        return generateLoginResponse(user.getId(), account, "user");
    }

    // 工具方法：判断密码是否已被BCrypt加密（BCrypt加密后以$2a$/$2b$/$2y$开头）
    private boolean isEncryptedPassword(String password) {
        return password != null && password.startsWith("$2");
    }

    // 封装Token生成逻辑
    private LoginResponse generateLoginResponse(Long id, String account, String role) {
        String token = jwtUtils.generateToken(id, account, role);
        return new LoginResponse(token, role, id);
    }
}