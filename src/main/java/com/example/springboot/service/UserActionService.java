package com.example.springboot.service;

import com.example.springboot.dto.UserActionResultDTO;

public interface UserActionService {
    UserActionResultDTO getUserActionInfo(Long userId);
}