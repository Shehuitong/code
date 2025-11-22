package com.example.springboot.Service;

import com.example.springboot.dto.LoginDTO;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.User;
import com.example.springboot.excption.LoginException;
import com.example.springboot.vo.LoginResponse;
import com.example.springboot.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtils;

    @Override
    public LoginResponse login(LoginDTO loginDTO) {
        String account = loginDTO.getAccount();
        String password = loginDTO.getPassword();

        // 1. 验证账号格式
        // 管理员账号格式：G2025 + 3位数字（例如G2025001）
        boolean isAdminAccount = account.matches("^G2025\\d{3}$");
        // 普通用户账号格式：4位年级 + 3位学院 + 2位座位（共9位数字，例如202501001）
        boolean isUserAccount = account.matches("^202[2-5]\\d{3}\\d{2}$");

        if (!isAdminAccount && !isUserAccount) {
            throw new LoginException("账号格式错误！");
        }

        // 2. 验证管理员登录（仅当账号符合管理员格式时）
        if (isAdminAccount) {
            Admin admin = adminService.getByWorkId(account);
            if (admin == null) {
                throw new LoginException("管理员账号不存在");
            }
            if (!passwordEncoder.matches(password, admin.getPassword())) {
                throw new LoginException("管理员密码错误");
            }
            String token = jwtUtils.generateToken(admin.getId(), admin.getWorkId(), "admin");
            return new LoginResponse(token, "admin", admin.getId());
        }

        // 3. 验证普通用户登录（仅当账号符合用户格式时）
        if (isUserAccount) {
            User user = userService.getByStudentId(account);
            if (user == null) {
                throw new LoginException("用户账号不存在");
            }
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new LoginException("用户密码错误");
            }
            String token = jwtUtils.generateToken(user.getId(), user.getStudentId(), "user");
            return new LoginResponse(token, "user", user.getId());
        }

        // 理论上不会走到这里（已通过格式校验）
        throw new LoginException("登录失败");
    }
}