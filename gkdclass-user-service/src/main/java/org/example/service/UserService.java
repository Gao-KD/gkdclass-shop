package org.example.service;

import org.example.request.UserLoginRequest;
import org.example.request.UserRegisterRequest;
import org.example.utils.JsonData;

public interface UserService {

    //用户注册
    JsonData Register(UserRegisterRequest userRegisterRequest);

    //用户登陆
    JsonData Login(UserLoginRequest userLoginRequest);
}
