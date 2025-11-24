package com.example.springboot.service;
import com.example.springboot.dto.LoginDTO; // 导入登录请求DTO
import com.example.springboot.vo.LoginResponse;



public interface LoginService {

        /**
         * 统一登录（自动识别管理员/普通用户）
         * @param loginDTO 登录请求参数（账号+密码）
         * @return 登录响应（token+角色+基本信息）
         */
        LoginResponse login(LoginDTO loginDTO);
    }
