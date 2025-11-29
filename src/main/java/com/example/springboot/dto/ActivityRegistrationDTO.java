// ActivityRegistrationDTO.java
package com.example.springboot.dto;

import com.example.springboot.enums.RegistrationStatusEnum;
import lombok.Data;

// 报名活动DTO（包含活动详情，匹配接口返回类型要求）
@Data
public class ActivityRegistrationDTO {
    private Long registrationId; // 报名记录ID
    private ActivityDetailDTO activity; // 活动详情（嵌套完整活动信息）
    private RegistrationStatusEnum registrationStatus; // 新增：报名状态（枚举）
}