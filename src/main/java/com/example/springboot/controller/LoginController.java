package com.example.springboot.controller;

import com.example.springboot.service.LoginService;
import com.example.springboot.common.Result;
import com.example.springboot.dto.LoginDTO;
import com.example.springboot.vo.LoginResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

// 构造器注入替代字段注入，解决“不建议使用字段注入”警告
@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;

    // 构造器注入（确保依赖初始化，避免循环依赖）
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * 统一登录接口（无需选择身份，自动识别）
     */
    @PostMapping
    public Result<LoginResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginResponse loginResponse = loginService.login(loginDTO);
        // 若Result.success支持双参数则保留，否则改为单参数（如Result.success(loginResponse)）
        return Result.success(loginResponse);

    }
}