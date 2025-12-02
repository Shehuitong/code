package com.example.springboot.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 从配置文件注入管理员头像存储路径，与AdminServiceImpl保持一致
    @Value("${admin.avatar.path:D:/campus/admin_avatar/}")
    private String adminAvatarPath;
    // 配置静态资源映射：适配管理员头像访问路径
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 管理员头像映射：URL路径/admin_avatar/** 对应本地存储目录
        registry.addResourceHandler("/admin_avatar/**")
                .addResourceLocations("file:" + adminAvatarPath);

        // 保留原有其他头像映射（如果有其他业务使用）
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:D:/campus/avatar/");
    }
}