package com.example.springboot.excption;

// 自定义业务异常，用于抛出具体业务错误
public class BusinessErrorException extends RuntimeException {
    public BusinessErrorException(String message) {
        super(message); // 保存错误信息
    }
}