package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门Mapper接口（继承MyBatis-Plus的BaseMapper，自动获得CRUD方法）
 */
@Mapper // 标记为MyBatis的Mapper接口，或通过@MapperScan扫描该包
public interface DepartmentMapper extends BaseMapper<Department> {
    // BaseMapper已包含selectById、selectOne、selectList等基础方法，无需重复定义
    // 如需自定义查询，可在此添加方法（如按部门ID查询）
}