// filePath: com/example/springboot/dto/ActivityEditDTO.java
package com.example.springboot.dto;

import com.example.springboot.enums.ScoreTypeEnum;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityEditDTO {

    @NotBlank(message = "活动名称不能为空")
    @Size(max = 255, message = "活动名称不能超过255个字符")
    private String activityName;

    @NotNull(message = "活动开始时间不能为空")
    private LocalDateTime holdStartTime;

    @NotNull(message = "活动结束时间不能为空")
    private LocalDateTime holdEndTime;

    @NotBlank(message = "活动举办地点不能为空")
    @Size(max = 255, message = "活动举办地点不能超过255个字符")
    private String location;

    private ScoreTypeEnum scoreType;

    @DecimalMin(value = "0.00", message = "加分分值不能为负数")
    private BigDecimal score;

    @DecimalMin(value = "0.00", message = "志愿时长不能为负数")
    private BigDecimal volunteerHours;

    @NotBlank(message = "活动介绍不能为空")
    @Size(max = 1000, message = "活动介绍不能超过1000个字符")
    private String activityDesc;
}