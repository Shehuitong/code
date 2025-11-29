package com.example.springboot.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.springboot.enums.CollegeEnum;
import com.example.springboot.enums.GradeEnum;
import lombok.Data; // Lombok注解，自动生成getter/setter等方法

/**
 * 普通用户实体类（对应数据库user表）
 */
@Data
@TableName("`user`") // 绑定数据库普通用户表（表名可根据实际调整）
public class User {
    //@TableId(value = "user_id", type = IdType.AUTO)
    @TableId(value = "id", type = IdType.AUTO)
    private Long Id;

    private String studentId;

    private String password; // 密码

    private String username; // 姓名

    @TableField("avatar_url")
    private String avatarUrl;//头像

    private CollegeEnum college; // 学院

    private GradeEnum grade; // 年级

    private String phone;
}
