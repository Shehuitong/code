package com.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 单个部门查询（给主键起别名 AS deptId，强制映射）
     */
    @Select("SELECT " +
            "department_id AS deptId, " + // 别名和实体字段名一致
            "department_name, " +
            "description, " +
            "logo_url " +
            "FROM Department " +
            "WHERE department_id = #{id}")
    Department selectById(@Param("id") Long id);

    /**
     * 批量部门查询（给主键起别名 AS deptId）
     */
    @Select("<script>" +
            "SELECT " +
            "department_id AS deptId, " + // 别名和实体字段名一致
            "department_name, " +
            "description, " +
            "logo_url " +
            "FROM Department " +
            "WHERE department_id IN " +
            "<foreach collection='ids' item='deptId' open='(' separator=',' close=')'>" +
            "   #{deptId}" +
            "</foreach>" +
            "</script>")
    List<Department> selectDeptWithName(@Param("ids") List<Long> ids);
}