package com.example.springboot.common;
import lombok.Data;

@Data
public class Result<T> {
    // 状态码（200=成功，400=请求错误，401=未授权等）
    private Integer code;
    // 提示信息
    private String message;
    // 返回数据
    private T data;

    // 成功返回（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 失败返回（带提示）
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMessage(message);
        return result;
    }

    public static Result<String> successLogin(String token) {
        Result<String> result = new Result<>();
        result.setCode(200);       // 固定成功状态码
        result.setMessage("登录成功");  // 固定登录成功消息
        result.setData(token);     // 传入生成的 JWT Token
        return result;
    }

}