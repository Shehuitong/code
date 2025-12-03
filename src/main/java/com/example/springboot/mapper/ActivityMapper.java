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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.enums.ActivityStatusEnum;
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
            d.department_id AS dept_id, d.department_name
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
            @Result(property = "departmentName", column = "department_name"),
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

    // 新增：单个ID查询活动+关联部门完整信息（用于详情页）
    @Select("SELECT a.*, d.* " +
            "FROM activity a " +
            "LEFT JOIN department d ON a.department_id = d.department_id " +
            "WHERE a.activity_id = #{activityId}")
    @Results({
            // 映射活动表字段（与Activity实体一致）
            @Result(column = "activity_id", property = "activityId"),
            @Result(column = "activity_name", property = "activityName"),
            @Result(column = "department_id", property = "departmentId"),
            @Result(column = "activity_desc", property = "activityDesc"),
            @Result(column = "hold_start_time", property = "holdStartTime"),
            @Result(column = "hold_end_time", property = "holdEndTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "apply_college", property = "applyCollege"),
            @Result(column = "apply_grade", property = "applyGrade"),
            @Result(column = "score_type", property = "scoreType"),
            @Result(column = "score", property = "score"),
            @Result(column = "max_people", property = "maxPeople"),
            @Result(column = "remaining_people", property = "remainingPeople"),
            @Result(column = "apply_count", property = "applyCount"),
            @Result(column = "apply_deadline", property = "applyDeadline"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_time", property = "createdTime"),
            @Result(column = "apply_time", property = "applyTime"),
            @Result(column = "volunteer_hours", property = "volunteerHours"),
            @Result(column = "follower_count", property = "followerCount"),
            @Result(column = "hold_college", property = "holdCollege"),

            // 映射部门表字段（关联到Activity实体的department属性）
            @Result(column = "department_id", property = "department.departmentId"),
            @Result(column = "department_name", property = "department.departmentName"),
            @Result(column = "description", property = "department.description"),
            @Result(column = "avatar", property = "department.avatar"),
            @Result(column = "department_college", property = "department.departmentCollege")
    })
    Activity selectActivityWithDeptById(@Param("activityId") Long activityId);
    /**
     * 分页查询活动并关联部门信息
     */
    @Select("""
        <script>
        SELECT a.*, d.*
        FROM Activity a
        LEFT JOIN Department d ON a.department_id = d.department_id
        WHERE a.activity_name LIKE CONCAT('%', #{keyword}, '%')
          AND a.status != #{offlineStatus}
        ORDER BY a.activity_id ASC
        </script>
    """)
    @Results({
            // 活动字段映射（同已有配置）
            @Result(column = "activity_id", property = "activityId"),
            @Result(column = "activity_name", property = "activityName"),
            @Result(column = "department_id", property = "departmentId"),
            @Result(column = "activity_desc", property = "activityDesc"),
            @Result(column = "hold_start_time", property = "holdStartTime"),
            @Result(column = "hold_end_time", property = "holdEndTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "apply_college", property = "applyCollege"),
            @Result(column = "apply_grade", property = "applyGrade"),
            @Result(column = "score_type", property = "scoreType"),
            @Result(column = "score", property = "score"),
            @Result(column = "max_people", property = "maxPeople"),
            @Result(column = "remaining_people", property = "remainingPeople"),
            @Result(column = "apply_count", property = "applyCount"),
            @Result(column = "apply_deadline", property = "applyDeadline"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_time", property = "createdTime"),
            @Result(column = "apply_time", property = "applyTime"),
            @Result(column = "volunteer_hours", property = "volunteerHours"),
            @Result(column = "follower_count", property = "followerCount"),
            @Result(column = "hold_college", property = "holdCollege"),

            // 部门字段映射
            @Result(column = "department_id", property = "department.departmentId"),
            @Result(column = "department_name", property = "department.departmentName"),
            @Result(column = "description", property = "department.description"),
            @Result(column = "avatar", property = "department.avatar"),
            @Result(column = "department_college", property = "department.departmentCollege")
    })
    IPage<Activity> selectActivityPageWithDept(
            @Param("page") Page<Activity> page,
            @Param("keyword") String keyword,
            @Param("offlineStatus") ActivityStatusEnum offlineStatus
    );

    /**
     * 查询所有未下架活动（关联部门信息）
     */
    @Select("""
    SELECT a.*, d.*
    FROM Activity a
    LEFT JOIN Department d ON a.department_id = d.department_id
    WHERE a.status != #{offlineStatus}
    ORDER BY a.activity_id ASC
""")
    @Results({
            // 活动字段映射（同已有配置）
            @Result(column = "activity_id", property = "activityId"),
            @Result(column = "activity_name", property = "activityName"),
            @Result(column = "department_id", property = "departmentId"),
            @Result(column = "activity_desc", property = "activityDesc"),
            @Result(column = "hold_start_time", property = "holdStartTime"),
            @Result(column = "hold_end_time", property = "holdEndTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "apply_college", property = "applyCollege"),
            @Result(column = "apply_grade", property = "applyGrade"),
            @Result(column = "score_type", property = "scoreType"),
            @Result(column = "score", property = "score"),
            @Result(column = "max_people", property = "maxPeople"),
            @Result(column = "remaining_people", property = "remainingPeople"),
            @Result(column = "apply_count", property = "applyCount"),
            @Result(column = "apply_deadline", property = "applyDeadline"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_time", property = "createdTime"),
            @Result(column = "apply_time", property = "applyTime"),
            @Result(column = "volunteer_hours", property = "volunteerHours"),
            @Result(column = "follower_count", property = "followerCount"),
            @Result(column = "hold_college", property = "holdCollege"),

            // 部门字段映射
            @Result(column = "department_id", property = "department.departmentId"),
            @Result(column = "department_name", property = "department.departmentName"),
            @Result(column = "description", property = "department.description"),
            @Result(column = "avatar", property = "department.avatar"),
            @Result(column = "department_college", property = "department.departmentCollege")
    })
    List<Activity> selectAllActivityWithDept(@Param("offlineStatus") ActivityStatusEnum offlineStatus);
    /**
     * 关键新增：全量查询关键词匹配的活动（关联部门，纯注解实现）
     */
    @Select("""
        SELECT a.*, d.*
        FROM Activity a
        LEFT JOIN Department d ON a.department_id = d.department_id
        WHERE a.activity_name LIKE CONCAT('%', #{keyword}, '%')
          AND a.status != #{offlineStatus}
        ORDER BY a.activity_id ASC
    """)
    @Results({
            // 活动字段映射（与已有方法完全一致）
            @Result(column = "activity_id", property = "activityId"),
            @Result(column = "activity_name", property = "activityName"),
            @Result(column = "department_id", property = "departmentId"),
            @Result(column = "activity_desc", property = "activityDesc"),
            @Result(column = "hold_start_time", property = "holdStartTime"),
            @Result(column = "hold_end_time", property = "holdEndTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "apply_college", property = "applyCollege"),
            @Result(column = "apply_grade", property = "applyGrade"),
            @Result(column = "score_type", property = "scoreType"),
            @Result(column = "score", property = "score"),
            @Result(column = "max_people", property = "maxPeople"),
            @Result(column = "remaining_people", property = "remainingPeople"),
            @Result(column = "apply_count", property = "applyCount"),
            @Result(column = "apply_deadline", property = "applyDeadline"),
            @Result(column = "status", property = "status"),
            @Result(column = "created_time", property = "createdTime"),
            @Result(column = "apply_time", property = "applyTime"),
            @Result(column = "volunteer_hours", property = "volunteerHours"),
            @Result(column = "follower_count", property = "followerCount"),
            @Result(column = "hold_college", property = "holdCollege"),

            // 部门字段映射（与已有方法完全一致）
            @Result(column = "department_id", property = "department.departmentId"),
            @Result(column = "department_name", property = "department.departmentName"),
            @Result(column = "description", property = "department.description"),
            @Result(column = "avatar", property = "department.avatar"),
            @Result(column = "department_college", property = "department.departmentCollege")
    })
    List<Activity> selectAllActivityWithDeptByKeyword(
            @Param("keyword") String keyword,
            @Param("offlineStatus") ActivityStatusEnum offlineStatus);
}
