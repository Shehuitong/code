package com.example.springboot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    // 处理根路径请求
    @GetMapping("/")
    public String home() {
        return "Spring Boot Demo 应用已启动成功！\n" +
                "H2控制台地址：http://localhost:8080/h2-console\n" +
                "数据库连接URL：jdbc:h2:mem:testdb\n" +
                "用户名：sa，密码：（空）";
    }
}