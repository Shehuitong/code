package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Activity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动Mapper接口（继承MyBatis-Plus的BaseMapper，自动获得CRUD方法）
 */
@Mapper // 标记为MyBatis的Mapper接口
public interface ActivityMapper extends BaseMapper<Activity> {
    // BaseMapper提供的核心方法：
    // selectList()：查询列表（支持条件构造器）
    // selectOne()：查询单个对象（支持条件构造器）
    // insert()/updateById()/deleteById()：增删改
    // 如需自定义查询（如按部门ID查活动），可在此添加方法
}