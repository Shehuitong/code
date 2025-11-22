-- 1. 创建 user 表
CREATE TABLE IF NOT EXISTS `user` (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      username VARCHAR(50) NOT NULL COMMENT '用户名',
    student_id VARCHAR(20) NOT NULL UNIQUE COMMENT '学生ID（唯一）',
    college VARCHAR(100) COMMENT '学院',
    grade VARCHAR(50) COMMENT '年级', -- 新增grade列
    phone VARCHAR(20) COMMENT '手机号',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    password VARCHAR(100) NOT NULL COMMENT '加密后的密码'
    );

-- 2. 创建 DepartmentAdmin 表及索引
CREATE TABLE `DepartmentAdmin` (
   admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  department_id INT NOT NULL COMMENT '部门ID（关联部门表）',
 employee_id VARCHAR(50) NOT NULL COMMENT '管理员工号',
  admin_password VARCHAR(255) NOT NULL COMMENT '管理员密码（存储加密后的值）',
 admin_name VARCHAR(100) NOT NULL COMMENT '管理员昵称',
  admin_avatar_url VARCHAR(255) DEFAULT NULL COMMENT '管理员头像URL地址'
);

-- 单独定义索引（H2推荐方式）
CREATE INDEX `idx_department_id` ON `DepartmentAdmin` (`department_id`);
CREATE UNIQUE INDEX `uk_employee_id` ON `DepartmentAdmin` (`employee_id`);

CREATE TABLE IF NOT EXISTS Activity (
                                        activity_id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 活动主键ID（自增）

                                        activity_name VARCHAR(255) NOT NULL,           -- 活动名称（非空）

    department_id INTEGER NOT NULL,                -- 关联部门ID（外键，非空）

    activity_desc VARCHAR(1000) NOT NULL,          -- 活动详细介绍（非空）

    hold_start_time DATETIME NOT NULL,             -- 活动开始时间（非空）

    hold_end_time DATETIME NOT NULL,               -- 活动结束时间（非空）

    location VARCHAR(255) NOT NULL,                -- 举办地点（非空）

    apply_college VARCHAR(50) NOT NULL,            -- 报名对象学院（非空，枚举，如 "COLLEGE_A"）

    score_type VARCHAR(50),                        -- 加分类型（可选，如 "ACADEMIC"）

    score DECIMAL(10, 2),                          -- 加分分值（可选）

    max_people INTEGER NOT NULL,                   -- 人数限制（非空，0表示无限制）

    remaining_people INTEGER NOT NULL,             -- 剩余名额（非空）

    apply_count INTEGER NOT NULL,                  -- 已报名人数（非空）

    apply_deadline DATETIME NOT NULL,              -- 报名截止时间（非空）

    status VARCHAR(50) NOT NULL,                   -- 活动状态（非空，如 "PENDING", "ONGOING", "ENDED"）

    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 发布时间（默认当前时间）

    apply_time DATETIME NOT NULL,                  -- 报名开始时间（非空）

    volunteer_hours DECIMAL(5, 2),                 -- 志愿时长（可选）

    follower_count INTEGER,                        -- 收藏人数（可选）

    hold_college VARCHAR(50) NOT NULL,             -- 承办学院（非空，如 "COLLEGE_B"）

    apply_grade VARCHAR(50) NOT NULL               -- 报名年级（非空，如 "GRADE_1"）
    );
CREATE TABLE IF NOT EXISTS UserActivityRegistration (
     registration_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '报名记录主键ID',
     id BIGINT NOT NULL COMMENT '关联用户表ID（外键）',
     activity_id BIGINT NOT NULL COMMENT '关联活动表ID（外键）',
     registration_status VARCHAR(20) NOT NULL COMMENT '报名状态（枚举值，如：REGISTERED, CANCELLED等）',
    CONSTRAINT fk_registration_user FOREIGN KEY (id) REFERENCES `user`(id),
    CONSTRAINT fk_registration_activity FOREIGN KEY (activity_id) REFERENCES `Activity`(activity_id),
    CONSTRAINT uk_user_activity UNIQUE (id, activity_id)
    );
