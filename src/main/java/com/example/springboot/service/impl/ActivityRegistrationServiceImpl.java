package com.example.springboot.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.dto.ActivityDetailDTO;
import com.example.springboot.dto.ActivityRegistrationDTO;
import com.example.springboot.dto.ActivityRegistrationExcelDTO;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.ActivityRegistration;
import com.example.springboot.entity.Department;
import com.example.springboot.entity.User;
import com.example.springboot.enums.*;
import com.example.springboot.excption.BusinessErrorException;
import com.example.springboot.mapper.ActivityMapper;
import com.example.springboot.mapper.ActivityRegistrationMapper;
import com.example.springboot.mapper.DepartmentMapper;
import com.example.springboot.service.ActivityRegistrationService;
import com.example.springboot.service.ActivityService;
import com.example.springboot.service.AdminService;
import com.example.springboot.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
// 假设你的 UserMapper 在 com.example.springboot.mapper 包下（重点看包路径）
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.mapper.ActivityRegistrationMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActivityRegistrationServiceImpl extends ServiceImpl<ActivityRegistrationMapper, ActivityRegistration>
        implements ActivityRegistrationService {


    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;
    private final AdminService adminService; // 管理员服务
    private final ActivityService activityService;
    private final DepartmentMapper departmentMapper;
    @Autowired  // 新增：注入JwtUtil
    private   JwtUtil  jwtUtil;  // 假设项目中存在JwtUtil工具类

    @Autowired
    public ActivityRegistrationServiceImpl(
            UserMapper userMapper,
            ActivityMapper activityMapper,
            AdminService adminService,
            ActivityService activityService,
            DepartmentMapper departmentMapper
    ) {
        this.userMapper = userMapper;
        this.activityMapper = activityMapper;
        this.adminService = adminService;
        this.activityService = activityService;
        this.departmentMapper = departmentMapper;
    }
    // 在ActivityRegistrationServiceImpl中添加以下代码

    @Override
    public List<Long> getUserIdsByActivityId(Long activityId) {
        // 校验活动ID是否存在
        if (activityId == null) {
            throw new IllegalArgumentException("活动ID不能为空");
        }
        // 查询该活动下所有报名记录的用户ID
        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityRegistration::getActivityId, activityId)
                .select(ActivityRegistration::getUserId); // 只查询userId字段，优化性能
        // 提取用户ID列表
        return baseMapper.selectList(wrapper).stream()
                .map(ActivityRegistration::getUserId)
                .distinct() // 去重（防止重复报名记录导致的重复用户ID）
                .toList();
    }
    /**
     * 查看用户已报名的活动（仅查状态码1，返回活动全部信息）
     * @param userId 用户ID
     * @return 已报名活动DTO列表（含报名信息+完整活动信息+部门信息）
     */
    @Override
    public List<ActivityRegistrationDTO> getMyRegisteredActivities(Long userId) {
        // 1. 基础校验
        if (userId == null) {
            log.error("查看已报名活动失败：用户ID为空（Token解析异常）");
            throw new BusinessErrorException("登录状态异常，请重新登录");
        }

        // 2. 查询用户「已报名」状态的报名记录（状态码1）
        LambdaQueryWrapper<ActivityRegistration> registrationWrapper = new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getUserId, userId) // 类型一致，无需转换
                .eq(ActivityRegistration::getRegistrationStatus, RegistrationStatusEnum.APPLIED.getCode());

        List<ActivityRegistration> registrationList = baseMapper.selectList(registrationWrapper);
        if (registrationList.isEmpty()) {
            log.info("用户{}暂无已报名的活动", userId);
            return Collections.emptyList();
        }

        // 3. 批量查询关联的活动信息（添加日志）
        Set<Long> activityIds = registrationList.stream()
                .map(ActivityRegistration::getActivityId)
                .collect(Collectors.toSet());
        log.info("待查询的活动ID集合：{}", activityIds); // 日志1：确认活动ID正确（应包含1、3）

        List<Activity> activityList = activityMapper.selectActivityWithDeptByIds(new ArrayList<>(activityIds));
        log.info("查询到的活动列表：{}", activityList.stream().map(a -> "活动ID：" + a.getActivityId() + "，部门ID：" + a.getDepartmentId()).collect(Collectors.toList())); // 日志2：确认活动的departmentId是否为1、3

        Map<Long, Activity> activityMap = activityList.stream()
                .collect(Collectors.toMap(Activity::getActivityId, activity -> activity));

        // 4. 批量查询活动关联的部门信息（添加日志）
        Set<Long> departmentIds = activityList.stream()
                .map(Activity::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        log.info("待查询的部门ID集合：{}", departmentIds); // 日志3：确认部门ID是否为1、3

        List<Department> departmentList = departmentMapper.selectDeptWithName(new ArrayList<>(departmentIds));
        log.info("查询到的部门列表：{}", departmentList.stream().map(d -> "部门ID：" + d.getDepartmentId() + "，部门名称：" + d.getDepartmentName()).collect(Collectors.toList())); // 日志4：确认部门是否查询到（应包含学生会、体育部）

        Map<Long, Department> deptMap = departmentList.stream()
                .filter(dept -> dept.getDepartmentId() != null)
                .collect(Collectors.toMap(Department::getDepartmentId, Function.identity(), (v1, v2) -> v1));
        log.info("deptMap的key集合（部门ID）：{}", deptMap.keySet()); // 日志5：确认deptMap包含1、3

        // 5. 组装DTO（添加日志）
        List<ActivityRegistrationDTO> resultDTOs = new ArrayList<>();
        for (ActivityRegistration registration : registrationList) {
            ActivityRegistrationDTO registrationDTO = new ActivityRegistrationDTO();
            registrationDTO.setRegistrationId(registration.getRegistrationId());
            registrationDTO.setRegistrationStatus(RegistrationStatusEnum.APPLIED);

            Activity activity = activityMap.get(registration.getActivityId());
            if (activity != null) {
                ActivityDetailDTO activityDetailDTO = new ActivityDetailDTO();
                BeanUtils.copyProperties(activity, activityDetailDTO);
                log.info("活动ID：{}，复制后的ActivityDetailDTO部门ID：{}", activity.getActivityId(), activityDetailDTO.getDepartmentId()); // 日志6：确认复制后的departmentId是否正确

                // 补充部门名称（添加日志）
                Department department = deptMap.get(activity.getDepartmentId());
                log.info("活动ID：{}，通过部门ID：{} 从deptMap获取到的部门：{}", activity.getActivityId(), activity.getDepartmentId(), department); // 日志7：确认是否能获取到部门

                activityDetailDTO.setDepartmentName(department != null ? department.getDepartmentName() : "未知部门");
                registrationDTO.setActivity(activityDetailDTO);
            } else {
                // 处理活动已删除的情况
                ActivityDetailDTO deletedActivity = new ActivityDetailDTO();
                deletedActivity.setActivityName("该活动已删除");
                registrationDTO.setActivity(deletedActivity);
                log.warn("用户{}的报名记录（ID：{}）关联的活动（ID：{}）已删除",
                        userId, registration.getRegistrationId(), registration.getActivityId());
            }

            resultDTOs.add(registrationDTO);
        }

        log.info("用户{}查询已报名活动成功，共{}条记录", userId, resultDTOs.size());
        return resultDTOs;
    }

//    @Override
//    public List<ActivityRegistrationDTO> getUserRegistrationDTOs(Long userId) {
//        return getMyRegisteredActivities(userId);
//    }

    @Override
    public int countByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessErrorException("用户ID不能为空");
        }

        LambdaQueryWrapper<ActivityRegistration> countWrapper = new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getUserId, userId) // 直接传 Long，类型匹配
                .eq(ActivityRegistration::getRegistrationStatus, RegistrationStatusEnum.APPLIED);

        return Math.toIntExact(baseMapper.selectCount(countWrapper));
    }

    @Override
    public List<ActivityRegistration> getRegistrationsByUserId(Long userId) {
        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getUserId, userId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public void exportRegisteredUsers(Long activityId, HttpServletResponse response) throws IOException {
        log.info("开始导出活动[{}]的已报名用户", activityId);

        // 1. 校验活动ID
        if (activityId == null || activityId <= 0) {
            log.error("活动ID无效：{}", activityId);
            throw new IllegalArgumentException("活动ID无效");
        }

        // 2. 查询活动（确认存在）
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.error("活动[{}]不存在", activityId);
            throw new RuntimeException("活动不存在");
        }


        // 3. 查询该活动的有效报名记录（重点：确认枚举映射正确）
        LambdaQueryWrapper<ActivityRegistration> registrationWrapper = new LambdaQueryWrapper<>();
        registrationWrapper.eq(ActivityRegistration::getActivityId, activityId)
                .eq(ActivityRegistration::getRegistrationStatus, RegistrationStatusEnum.APPLIED); // 确保枚举的dbValue与数据库一致
        List<ActivityRegistration> registrations = baseMapper.selectList(registrationWrapper);
        log.info("活动[{}]查询到的有效报名记录数：{}", activityId, registrations.size()); // 新增日志：确认报名记录数量
        if (CollectionUtils.isEmpty(registrations)) {
            log.info("活动[{}]暂无已报名用户", activityId);
            throw new RuntimeException("暂无报名数据");
        }

        // 4. 批量查询关联的用户信息（修复：确保用户ID集合正确）
        Set<Long> userIds = registrations.stream()
                .map(ActivityRegistration::getUserId)
                .filter(Objects::nonNull) // 过滤空用户ID
                .collect(Collectors.toSet());
        log.info("报名记录关联的用户ID集合：{}", userIds); // 新增日志：确认用户ID是否有效
        if (userIds.isEmpty()) {
            log.error("活动[{}]的报名记录中用户ID全部为空", activityId);
            throw new RuntimeException("用户数据异常：无有效用户ID");
        }

        // 修复：使用正确的用户查询方法（确保userMapper实现了selectBatchIds）
        List<User> users = userMapper.selectBatchIds(userIds);
        log.info("根据用户ID查询到的用户数量：{}", users.size()); // 新增日志：确认用户查询结果
        if (CollectionUtils.isEmpty(users)) {
            log.error("活动[{}]的报名记录关联用户不存在", activityId);
            throw new RuntimeException("用户数据异常：未查询到用户");
        }

        // 转为Map便于查询：userId -> User
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (v1, v2) -> v1));
        log.info("用户Map中包含的用户ID：{}", userMap.keySet()); // 新增日志：确认用户Map有效

        // 5. 组装Excel导出DTO（修复：不跳过无效用户，标记异常信息）
        List<ActivityRegistrationExcelDTO> excelDTOs = new ArrayList<>();
        for (int i = 0; i < registrations.size(); i++) {
            ActivityRegistration registration = registrations.get(i);
            User user = userMap.get(registration.getUserId());

            ActivityRegistrationExcelDTO dto = new ActivityRegistrationExcelDTO();
            dto.setSerialNumber(i + 1); // 序号从1开始

            if (user == null) {
                // 修复：不跳过，标记用户不存在
                log.warn("报名记录[{}]关联的用户[{}]不存在", registration.getRegistrationId(), registration.getUserId());
                dto.setUserName("用户不存在");
                dto.setStudentId("未知");
                dto.setCollege("未知");
                dto.setGrade("未知");
                dto.setPhone("未知");
            } else {
                // 正常填充用户信息
                dto.setUserName(user.getUsername());
                dto.setStudentId(user.getStudentId());
                dto.setCollege(user.getCollege() != null ? user.getCollege().getDesc() : "未知学院"); // 处理空学院
                dto.setGrade(user.getGrade() != null ? user.getGrade().getDesc() : "未知年级"); // 处理空年级
                dto.setPhone(user.getPhone() != null ? user.getPhone() : "未填写"); // 处理空手机号
            }
            excelDTOs.add(dto);
        }
        log.info("最终导出的DTO数量：{}", excelDTOs.size()); // 新增日志：确认DTO数量
        if (excelDTOs.isEmpty()) {
            log.error("活动[{}]的报名数据处理后为空", activityId);
            throw new RuntimeException("处理后无有效数据");
        }

        // 6. 设置响应头（确保编码正确）
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(activity.getActivityName() + "报名用户信息", StandardCharsets.UTF_8.name()) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        // 7. 写入Excel（确保流操作正确）
        try (OutputStream os = response.getOutputStream()) {
            // 核心：outputStream（响应流）、DTO.class（字段映射）、sheet名称（非空）
            EasyExcel.write(response.getOutputStream(), ActivityRegistrationExcelDTO.class)
                    .sheet("活动报名用户") // 1. 必须指定 Sheet 名称（不能为 null/空字符串）
                    .doWrite(excelDTOs);  // 2. 传入组装好的 3 条 DTO 列表
            log.info("活动[{}]的报名用户导出成功，共{}条数据", activityId, excelDTOs.size());
        } catch (IOException e) {
            log.error("活动[{}]的报名用户导出失败", activityId, e);
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }




    @Override
    public List<ActivityRegistrationDTO> getUserRegistrationDTOs(Long currentUserId) {
        LambdaQueryWrapper<ActivityRegistration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityRegistration::getUserId, currentUserId)
                .eq(ActivityRegistration::getRegistrationStatus, RegistrationStatusEnum.APPLIED);
        List<ActivityRegistration> registrations = baseMapper.selectList(queryWrapper);
        if (registrations.isEmpty()) {
            return List.of();
        }
        List<Long> activityIds = registrations.stream()
                .map(ActivityRegistration::getActivityId)
                .distinct()
                .collect(Collectors.toList());
        List<Activity> activities = activityService.listByIds(activityIds);
        Map<Long, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getActivityId, activity -> activity, (k1, k2) -> k1));

        // 新增日志1：打印活动对应的部门ID列表（验证是否有有效部门ID）
        List<Long> deptIds = activities.stream()
                .map(Activity::getDepartmentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        log.info("用户[{}]已报名活动对应的部门ID列表：deptIds={}", currentUserId, deptIds);

        // 新增日志2：打印批量查询的部门结果（验证是否查询到部门、部门名称是否非空）
        Map<Long, String> deptNameMap;
        if (!deptIds.isEmpty()) {
            List<Department> depts = departmentMapper.selectBatchIds(deptIds);
            log.info("批量查询部门结果：deptIds={}, 部门列表={}", deptIds, depts); // 看部门对象是否有值
            deptNameMap = depts.stream()
                    .collect(Collectors.toMap(
                            Department::getDepartmentId,
                            dept -> {
                                // 新增日志3：打印每个部门的ID和名称（验证部门名称是否存在）
                                log.info("部门ID={}, 部门名称={}", dept.getDepartmentId(), dept.getDepartmentName());
                                return dept.getDepartmentName();
                            },
                            (k1, k2) -> k1
                    ));
        } else {
            deptNameMap = new HashMap<>();
        }

        return registrations.stream()
                .map(registration -> {
                    ActivityRegistrationDTO dto = new ActivityRegistrationDTO();
                    dto.setRegistrationId(registration.getRegistrationId());
                    dto.setRegistrationStatus(registration.getRegistrationStatus());

                    Activity activity = activityMap.get(registration.getActivityId());
                    if (activity == null) {
                        throw new BusinessErrorException("报名记录关联的活动不存在：" + registration.getActivityId());
                    }

                    ActivityDetailDTO activityDTO = new ActivityDetailDTO();
                    BeanUtils.copyProperties(activity, activityDTO);
                    // 这里可以加个日志，验证当前活动的departmentId和deptNameMap中的值
                    log.info("活动ID={}, 关联部门ID={}, 匹配到的部门名称={}",
                            activity.getActivityId(), activity.getDepartmentId(),
                            deptNameMap.get(activity.getDepartmentId()));
                    activityDTO.setDepartmentName(deptNameMap.getOrDefault(activity.getDepartmentId(), "未知部门"));
                    dto.setActivity(activityDTO);

                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Transactional
    @Override
    public void registerActivity(Long userId, Long activityId) {
        // 1. 从请求头获取Token并解析角色（管理员判断核心逻辑）
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 移除Bearer前缀
        }

        // 从Token中解析角色信息，判断是否为管理员
        String role = jwtUtil.getRoleFromToken(token);
        if ("admin".equals(role)) {
            throw new BusinessErrorException("管理员不可参与活动报名");
        }
        // 获取用户信息，已知用户一定存在
        User user = userMapper.selectById(userId);
        // 悲观锁查询活动（防止并发超卖），已知活动一定存在
        Activity activity = activityMapper.selectByIdForUpdate(activityId);

        // 校验名额是否充足
        if (activity.getRemainingPeople() <= 0) {
            throw new BusinessErrorException("活动名额已满，无法报名");
        }

        // 校验学院是否符合
        ApplyCollegeEnum requiredCollege = activity.getApplyCollege();
        CollegeEnum userCollege = user.getCollege();
        if (!"全校".equals(requiredCollege.getDesc()) && !requiredCollege.name().equals(userCollege.name())) {
            throw new BusinessErrorException(String.format(
                    "您的学院（%s）不符合活动要求（%s）",
                    userCollege.getDesc(), requiredCollege.getDesc()
            ));
        }

        // 校验年级是否符合（假设Activity有requiredGrade字段，User有grade字段）
        GradeEnum requiredGrade = activity.getApplyGrade(); // 获取活动要求的年级（枚举类型）
        GradeEnum userGrade = user.getGrade(); // 假设User类中grade字段为GradeEnum类型
        // 若活动要求不是"全年级"，则校验用户年级是否匹配
        if (!"全年级".equals(requiredGrade.getDesc()) && !requiredGrade.equals(userGrade)) {
            throw new BusinessErrorException(String.format(
                    "您的年级（%s）不符合活动要求（%s）",
                    userGrade.getDesc(), requiredGrade.getDesc()
            ));
        }

        // 校验是否已报名
        LambdaQueryWrapper<ActivityRegistration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityRegistration::getUserId, userId)
                .eq(ActivityRegistration::getActivityId, activityId);
        ActivityRegistration existing = baseMapper.selectOne(queryWrapper);

        if (existing != null) {
            if (RegistrationStatusEnum.APPLIED.equals(existing.getRegistrationStatus())) {
                throw new BusinessErrorException("您已报名该活动");
            } else if (RegistrationStatusEnum.CANCELLED.equals(existing.getRegistrationStatus())) {
                // 恢复报名时再次检查名额
                existing.setRegistrationStatus(RegistrationStatusEnum.APPLIED);
                baseMapper.updateById(existing);
                activity.setApplyCount(activity.getApplyCount() + 1);
                activity.setRemainingPeople(activity.getRemainingPeople() - 1);
                activityMapper.updateById(activity);
            }
        } else {
            // 新增报名记录并减少名额
            ActivityRegistration registration = new ActivityRegistration();
            registration.setUserId(userId);
            registration.setActivityId(activityId);
            registration.setRegistrationStatus(RegistrationStatusEnum.APPLIED);
            baseMapper.insert(registration);
            activity.setApplyCount(activity.getApplyCount() + 1);
            activity.setRemainingPeople(activity.getRemainingPeople() - 1);
            activityMapper.updateById(activity);
        }
    }

    @Transactional
    @Override
    public void cancelRegistration(Long userId, Long activityId) {
        // 查询报名记录
        LambdaQueryWrapper<ActivityRegistration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityRegistration::getUserId, userId)
                .eq(ActivityRegistration::getActivityId, activityId)
                .eq(ActivityRegistration::getRegistrationStatus, RegistrationStatusEnum.APPLIED);
        ActivityRegistration registration = baseMapper.selectOne(queryWrapper);

        if (registration == null) {
            throw new BusinessErrorException("未找到报名记录，无法取消");
        }

        // 校验是否已取消
        if (RegistrationStatusEnum.CANCELLED.equals(registration.getRegistrationStatus())) {
            throw new BusinessErrorException("已取消报名，无需重复操作");
        }

        // 更新状态并增加名额
        registration.setRegistrationStatus(RegistrationStatusEnum.CANCELLED);
        baseMapper.updateById(registration);

        // 恢复活动名额
        Activity activity = activityMapper.selectById(activityId);
        activity.setApplyCount(activity.getApplyCount() - 1);
        activity.setRemainingPeople(activity.getRemainingPeople() + 1);
        activityMapper.updateById(activity);
    }
}