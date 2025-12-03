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
      --college VARCHAR(100) NOT NULL COMMENT '所属学院'
);

-- 单独定义索引（H2推荐方式）
CREATE INDEX `idx_department_id` ON `DepartmentAdmin` (`department_id`);
CREATE UNIQUE INDEX `uk_employee_id` ON `DepartmentAdmin` (`employee_id`);
--CREATE INDEX `idx_college` ON `DepartmentAdmin` (`college`);

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
-- 1. 创建部门表（H2语法，移除末尾COMMENT，改用单独注释语句）
CREATE TABLE IF NOT EXISTS Department (
                                          department_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
                                          department_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    description VARCHAR(200) COMMENT '部门描述',
    logo_url VARCHAR(255) DEFAULT 'https://picsum.photos/id/20/200' COMMENT '部门头像URL（对应查询中的logo_url）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    department_college VARCHAR(100) NOT NULL COMMENT '所属学院'
    );

-- 2. 为部门表添加注释（H2支持的表注释语法）
COMMENT ON TABLE Department IS '部门表';

--收藏关注表
CREATE TABLE user_favorites (
                                favorite_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id BIGINT,
                                target_id BIGINT,
                                target_type VARCHAR(255),
                                favorite_status VARCHAR(255),
                                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                is_deleted INT DEFAULT 0
);

-- 插入测试数据：覆盖「收藏部门」「关注活动」「已取消收藏」三种场景
INSERT INTO user_favorites (user_id, target_id, target_type, favorite_status, create_time, is_deleted)
VALUES
    -- 用户ID=3 收藏 部门ID=1（外国语学院-学生会）
    (1, 2, 'DEPARTMENT', '已取消', '2025-11-20 10:30:00', 0),
    -- 用户ID=3 收藏 部门ID=3（外国语学院-志愿者协会）
    (1, 3, 'DEPARTMENT', '已收藏', '2025-11-21 14:15:00', 0),
    -- 用户ID=3 关注 活动ID=2（英语演讲比赛）
    (1, 2, 'ACTIVITY', '已收藏', '2025-11-22 09:45:00', 0),
    -- 用户ID=3 关注 活动ID=4（跨文化交流沙龙）
    (3, 4, 'ACTIVITY', '已收藏', '2025-11-23 16:20:00', 0),
    -- 用户ID=3 取消收藏 部门ID=5（已取消状态）
    (3, 2, 'DEPARTMENT', '已收藏', '2025-11-24 11:00:00', 0),
    -- 用户ID=2 收藏 部门ID=1（其他用户的收藏数据，用于测试权限隔离）
    (4, 2, 'DEPARTMENT', '已收藏', '2025-11-25 13:30:00', 0);
    -- 用户ID=2 关注 活动ID=3（其他用户的关注数据）


-- 插入第一条：大学生程序设计竞赛
INSERT INTO Activity (
    activity_name, department_id, activity_desc, hold_start_time, hold_end_time,
    location, apply_college, score_type, score, max_people,
    remaining_people, apply_count, apply_deadline, status,
    created_time, apply_time, volunteer_hours, follower_count, hold_college, apply_grade
) VALUES (
             '大学生程序设计竞赛', 2, '面向全校学生的编程竞赛，提升实战能力...', '2025-06-10 09:00:00', '2025-06-10 17:00:00',
             '计算机学院报告厅', '全校', '1.1.科技竞赛', 2.00, 50,
             25, 25, '2025-12-31 23:59:59', '报名进行中',
             '2025-05-20 10:00:00', '2025-12-02 10:10:00', 2, 84, '计算机学院', '2025级'
         );

-- 插入第二条：数学建模讲座
INSERT INTO Activity (
    activity_name, department_id, activity_desc, hold_start_time, hold_end_time,
    location, apply_college, score_type, score, max_people,
    remaining_people, apply_count, apply_deadline, status,
    created_time, apply_time, volunteer_hours, follower_count, hold_college, apply_grade
) VALUES (
             '数学建模讲座', 2, '数学建模技巧分享，涵盖竞赛与科研应用...', '2025-06-15 14:00:00', '2025-06-15 16:00:00',
             '图书馆报告厅', '全校', '1.4.人文与科学素养', 5, 100,
             80, 20, '2025-06-10 23:59:59', '报名进行中',
             '2025-05-22 15:00:00', '2025-06-02 00:00:00', 0.5, 45, '计算机学院', '2024级+2025级'
         );

-- 插入第三条：志愿者招募活动
INSERT INTO Activity (
    activity_name, department_id, activity_desc, hold_start_time, hold_end_time,
    location, apply_college, score_type, score, max_people,
    remaining_people, apply_count, apply_deadline, status,
    created_time, apply_time, volunteer_hours, follower_count, hold_college, apply_grade
) VALUES (
             '志愿者招募活动', 2, '社区服务志愿者招募，累计志愿时长可加分...', '2025-06-20 08:00:00', '2025-06-20 18:00:00',
             '学校大礼堂', '全校', '1.1.科技竞赛', 10.00, 200,
             150, 50, '2025-12-29 23:59:59', '报名进行中',
             '2025-05-25 09:00:00', '2025-12-3 00:40:00', 1, 100, '电气学院', '2022级+2024级');

-- 插入第四条：大学生程序设计竞赛
INSERT INTO Activity (
    activity_name, department_id, activity_desc, hold_start_time, hold_end_time,
    location, apply_college, score_type, score, max_people,
    remaining_people, apply_count, apply_deadline, status,
    created_time, apply_time, volunteer_hours, follower_count, hold_college, apply_grade
) VALUES (
             '英语竞赛', 4, '面向全校学生的英语竞赛，提升实战能力...', '2025-06-10 09:00:00', '2025-06-10 17:00:00',
             '教学楼', '全校', '1.1.科技竞赛', 2.00, 50,
             25, 25, '2025-06-05 23:59:59', '报名进行中',
             '2025-05-20 10:00:00', '2025-06-01 00:00:00', 2, 84, '计算机学院', '2022级+2025级'
         );
 INSERT INTO DEPARTMENT ( DEPARTMENT_ID, DEPARTMENT_NAME, DESCRIPTION, LOGO_URL, CREATE_TIME,DEPARTMENT_COLLEGE) VALUES
(1, '学生会', '负责计算机科学与技术、软件工程等专业教学与科研', 'https://logo.cs.edu/computer.png', '2025-09-01 08:00:00','计算机学院'),
(2, '外联部', '涵盖英语、日语、法语等语种教学，培养涉外人才', 'https://logo.cs.edu/foreign.png', '2025-09-01 08:30:00','外国语学院'),
(3, '体育部', '专注数学理论与应用研究，开设数学与应用数学、统计学专业', NULL, '2025-09-01 09:00:00','机械学院'),
(4, '校团委', '从事基础物理与应用物理研究，含光电、新能源方向', 'https://logo.cs.edu/physics.png', '2025-09-01 09:30:00','电气学院'),
(5, '宣传部', '宣传部门活动', NULL, '2025-09-01 09:30:00','法学院');


INSERT INTO DEPARTMENTADMIN (DEPARTMENT_ID, EMPLOYEE_ID, ADMIN_PASSWORD, ADMIN_NAME, ADMIN_AVATAR_URL
) VALUES
      ( 1, 'G2025001', 'Admin123', '张管理员', 'https://avatar.admin.com/1.jpg'),
      ( 2, 'G2025002', 'Admin123', '李管理员', NULL),
      ( 3, 'G2025003', 'Admin123', '王管理员', 'https://avatar.admin.com/2.jpg'),
      ( 4, 'G2025004', 'Admin123', '赵管理员', NULL);

-- 插入USER表数据
INSERT INTO "USER" (USERNAME, STUDENT_ID, COLLEGE, GRADE, PHONE, AVATAR_URL, PASSWORD) VALUES
-- 2022级 外国语学院（001） 座位01
('张三', '202200101', '外国语学院', '2022级', '13800138001', NULL, 'Aa202200101'),
-- 2023级 计算机学院（002） 座位08
('李四', '202300208', '计算机学院', '2023级', '13900139002', 'https://avatar.default.com/1.jpg', 'Aa202300208'),
-- 2024级 数学学院（003） 座位15
('王五', '202400315', '机械学院', '2024级', '13700137003', NULL, 'Aa202400315'),
-- 2025级 物理学院（004） 座位22
('赵六', '202500422', '电气学院', '2025级', '13600136004', 'https://avatar.default.com/2.jpg', 'Aa202500422'),
-- 2022级 文学院（005） 座位30
('孙七', '202200530', '外国语学院', '2022级', '13500135005', NULL, 'Aa202200530'),
-- 2023级 外国语学院（001） 座位12
('周八', '202300112', '化学学院', '2023级', '13400134006', 'https://avatar.default.com/3.jpg', 'Aa202300112');

INSERT INTO USERACTIVITYREGISTRATION (ID, ACTIVITY_ID, REGISTRATION_STATUS)
VALUES
    ('1', 1, '已报名'),
    ('3', 1, '已报名'),
('2', 2, '已取消'),
    ('1', 2, '已取消');

CREATE TABLE IF NOT EXISTS notification (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            user_id BIGINT NOT NULL COMMENT '用户ID',
                                            content VARCHAR(255) NOT NULL COMMENT '通知内容',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_read INT DEFAULT 0 COMMENT '是否已读（0=未读，1=已读）',
    FOREIGN KEY (user_id) REFERENCES `user`(id)
    );

-- 活动提醒日志表：记录用户接收活动报名提醒的历史，用于去重
CREATE TABLE IF NOT EXISTS reminder_log (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
                                            user_id BIGINT NOT NULL COMMENT '用户ID（关联user表）',
                                            activity_id BIGINT NOT NULL COMMENT '活动ID（关联Activity表）',
                                            send_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '提醒发送时间',
                                            remark VARCHAR(255) COMMENT '备注信息',
    -- 外键约束（可选，根据实际需求是否启用）
    FOREIGN KEY (user_id) REFERENCES `user`(id),
    FOREIGN KEY (activity_id) REFERENCES Activity(activity_id)
    );
