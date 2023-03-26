package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.request.UserRegisterRequest;
import org.example.service.UserService;
import org.example.utils.JsonData;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Override
    public JsonData Register(UserRegisterRequest userRegisterRequest) {
        return null;
    }
}
