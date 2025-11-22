package com.example.springboot.excption;

public class LoginException extends RuntimeException { // 继承RuntimeException（属于Throwable）
    public LoginException(String message) {
        super(message);
    }
}
