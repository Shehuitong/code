package com.example.springboot.excption;

import com.example.springboot.common.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

// 全局异常处理器：捕获所有控制器层抛出的异常
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理参数校验异常（如@Valid校验失败）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMsg = fieldErrors.stream()
                .map(FieldError::getDefaultMessage) // 提取字段校验错误信息
                .collect(Collectors.joining("；")); // 多个错误用分号拼接
        return Result.error(errorMsg); // 返回错误信息
    }

    // 处理自定义业务异常（如"用户不存在"、"格式错误"等）
    @ExceptionHandler(BusinessErrorException.class)
    public Result<?> handleBusinessException(BusinessErrorException e) {
        return Result.error(e.getMessage()); // 直接返回业务错误信息
    }

    // 处理其他未捕获的异常（如空指针、IO异常等）
    @ExceptionHandler(Exception.class)
    public Result<?> handleOtherExceptions(Exception e) {
        // 实际项目中建议记录日志，此处简化处理
        return Result.error("系统异常：" + e.getMessage());
    }
}