package com.example.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 配置静态资源映射：将URL路径/avatar/**映射到本地头像存储目录
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 注意：Windows路径使用"file:D:/"，Linux/Mac使用"file:/home/"
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:D:/campus/avatar/");
    }
}