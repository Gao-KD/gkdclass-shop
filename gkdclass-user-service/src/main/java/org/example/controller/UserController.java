package org.example.controller;


import com.mysql.cj.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.BizCodeEnum;
import org.example.enums.SendCodeEnum;
import org.example.mapper.UserMapper;
import org.example.model.UserDO;
import org.example.request.UserRegisterRequest;
import org.example.service.NotifyService;
import org.example.service.UserService;
import org.example.utils.JsonData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

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

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotifyService notifyService;

    @ApiOperation("用户注册")
    @PostMapping("register")
    public JsonData register(@ApiParam("用户注册对象") @RequestBody UserRegisterRequest userRegisterRequest){

        boolean checkCode = false;
        if (!StringUtils.isNullOrEmpty(userRegisterRequest.getMail())){
            checkCode = notifyService.checkCode(SendCodeEnum.USER_REGISTER,userRegisterRequest.getMail(),userRegisterRequest.getCode());
            log.info("验证码校验结果:"+checkCode);
            if (!checkCode){
                return JsonData.buildResult(BizCodeEnum.CODE_ERROR);
            }
        }
        UserDO userDO = new UserDO();
        //把userRegisterRequest对象的属性拷贝到userDO
        BeanUtils.copyProperties(userRegisterRequest, userDO);
        userDO.setCreateTime(new Date());
        //设置密码 TODO


        //唯一性检查 TODO
        if (checkUnique(userDO.getMail())){
            int rows = userMapper.insert(userDO);
            log.info("影响行数:"+rows+",用户信息:"+userDO.toString());

            //用户注册后，初始化信息，发放优惠券等  TODO
            userRegisterInitTask(userDO);
            return JsonData.buildSuccess();
        }else {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_REPEAT);
        }
    }

    /**
     * 校验用户账号唯一
     * @param mail
     * @return
     */
    private boolean checkUnique(String mail) {
        return false;
    }


    /**
     * 用户注册，初始化福利 TODO
     */
    private void userRegisterInitTask(UserDO userDO){

    }
}

