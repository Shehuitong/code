//package com.example.springboot.mapper;
//
//import com.example.springboot.entity.Activity;
//import com.example.springboot.entity.Department;
//import com.baomidou.mybatisplus.core.mapper.BaseMapper;
//import org.apache.ibatis.annotations.*;
//import java.util.List;
//
///**
// * 活动Mapper（纯注解，修复动态SQL <foreach> 绑定问题）
// */
//@Mapper
//public interface ActivityMapper extends BaseMapper<Activity> {
//
//    /**
//     * 批量查询活动+关联部门信息（关键：用<script>包裹动态SQL）
//     */
//    @Select("""
//    <script>
//    SELECT a.* FROM Activity a
//    WHERE a.activity_id IN
//        <foreach collection="ids" item="item" open="(" separator="," close=")">
//            #{item}
//        </foreach>
//    </script>
//""")
//    @Results({
//            // 活动表字段映射（驼峰自动匹配）
//            @Result(property = "activityId", column = "activity_id"),
//            @Result(property = "activityName", column = "activity_name"),
//            @Result(property = "departmentId", column = "department_id"),
//            // 关联部门信息映射
//            @Result(
//                    property = "department",
//                    javaType = Department.class,
//                    column = "department_id", // 传递活动表的 department_id（单个 Long）
//                    one = @One(select = "com.example.springboot.mapper.DepartmentMapper.selectById") // 调用单个查询方法
//            )
//    })
//    List<Activity> selectActivityWithDeptByIds(@Param("ids") List<Long> ids);
//}
package com.example.springboot.mapper;

import com.example.springboot.entity.Activity;
import com.example.springboot.entity.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 活动Mapper（纯注解，修复动态SQL <foreach> 绑定问题）
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 批量查询活动+关联部门信息（关键：用<script>包裹动态SQL）
     */
    @Select("""
        <script>  <!-- 必须加！启用MyBatis动态SQL解析，识别foreach标签 -->
        SELECT
            a.*,
            d.department_id AS dept_id
        FROM Activity a
        LEFT JOIN Department d 
            ON a.department_id = d.department_id
        WHERE a.activity_id IN 
            <foreach collection="ids" item="item" open="(" separator="," close=")">
                #{item}
            </foreach>
        </script>
    """)
    @Results({
            // 活动表字段映射（驼峰自动匹配）
            @Result(property = "activityId", column = "activity_id"),
            @Result(property = "activityName", column = "activity_name"),
            @Result(property = "departmentId", column = "department_id"),
            // 关联部门信息映射
            @Result(
                    property = "department",
                    javaType = Department.class,
                    column = "departmentId",
                    one = @One(select = "com.example.springboot.mapper.DepartmentMapper.selectDeptWithName")
            )
    })
    List<Activity> selectActivityWithDeptByIds(@Param("ids") List<Long> ids);
    @Select("SELECT * FROM Activity WHERE activity_id = #{id} FOR UPDATE")
    Activity selectByIdForUpdate(@Param("id") Long id);
}