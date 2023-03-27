package org.example.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mysql.cj.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.example.enums.BizCodeEnum;
import org.example.enums.SendCodeEnum;
import org.example.mapper.UserMapper;
import org.example.model.UserDO;
import org.example.request.UserLoginRequest;
import org.example.request.UserRegisterRequest;
import org.example.service.NotifyService;
import org.example.service.UserService;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Gaokd
 * @since 2023-03-16
 */
@Api(tags = "用户模块")
@RestController
@RequestMapping("/api/user/v1")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;


    @ApiOperation("用户注册")
    @PostMapping("register")
    public JsonData register(@ApiParam("用户注册对象") @RequestBody UserRegisterRequest userRegisterRequest) {
        JsonData register = userService.Register(userRegisterRequest);
        return register;
    }

    /**
     * 用户登陆
     */
    @ApiOperation("用户登陆")
    @PostMapping("login")
    public JsonData userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        JsonData login = userService.Login(userLoginRequest,request);
        return login;
    }

}

