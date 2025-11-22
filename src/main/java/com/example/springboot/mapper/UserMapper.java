package com.example.springboot.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.User;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.example.springbootdemo.mapper")
public interface UserMapper extends BaseMapper<User> {

}
