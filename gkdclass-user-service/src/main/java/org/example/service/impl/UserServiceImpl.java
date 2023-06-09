package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.example.enums.BizCodeEnum;
import org.example.enums.SendCodeEnum;
import org.example.fegin.CouponFeginService;
import org.example.mapper.UserMapper;
import org.example.model.LoginUser;
import org.example.model.UserDO;
import org.example.request.NewUserCouponRequest;
import org.example.request.UserLoginRequest;
import org.example.request.UserRegisterRequest;
import org.example.service.NotifyService;
import org.example.service.UserService;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;
import org.example.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private CouponFeginService couponFeginService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotifyService notifyService;

    @Override
    public JsonData Register(UserRegisterRequest userRegisterRequest) {
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
        userDO.setSlogan("人生需要动态规划，学习需要贪心算法");
        //设置密码
        //生成密钥，盐
        userDO.setSecret("$1$"+ CommonUtil.getStringNumRandom(8));
        //盐+密码拼接
        String cryptPwd = Md5Crypt.md5Crypt(userRegisterRequest.getPwd().getBytes(), userDO.getSecret());
        userDO.setPwd(cryptPwd);

        //唯一性检查
        if (checkUnique(userDO.getMail())){
            int rows = userMapper.insert(userDO);
            log.info("影响行数:"+rows+",用户信息:"+userDO.toString());

            //用户注册后，初始化信息，发放优惠券等
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
        QueryWrapper queryWrapper = new QueryWrapper<UserDO>().eq("mail",mail);
        List<UserDO> list = userMapper.selectList(queryWrapper);

        return list.size() > 0 ? false : true;
    }


    /**
     * 用户注册，初始化福利
     */
    private void userRegisterInitTask(UserDO userDO){
        NewUserCouponRequest request = new NewUserCouponRequest();
        request.setUserId(userDO.getId());
        request.setName(userDO.getName());
        JsonData jsonData = couponFeginService.addNewUserCoupon(request);
        log.info("发放新用户注册优惠券:{},结果:{}",request.toString(),jsonData.toString());
    }

    /**
     * 用户登陆
     * @param userLoginRequest
     * @return
     */
    @Override
    public JsonData Login(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        boolean checkCode = false;
        if (!StringUtils.isNullOrEmpty(userLoginRequest.getMail())){
            checkCode = notifyService.checkCode(SendCodeEnum.USER_LOGIN,userLoginRequest.getMail(),userLoginRequest.getCode());
            if (!checkCode){
                return JsonData.buildResult(BizCodeEnum.CODE_ERROR);
            }
        }
        QueryWrapper queryWrapper = new QueryWrapper<UserDO>().eq("mail", userLoginRequest.getMail());
        UserDO userDO = userMapper.selectOne(queryWrapper);
        if (userDO != null){
            String secret = userDO.getSecret();
            String checkPwd = Md5Crypt.md5Crypt(userLoginRequest.getPwd().getBytes(), secret);
            log.info("用户信息:"+userDO.toString());
            if (checkPwd.equals(userDO.getPwd())){


                /**
                 * 生成Token
                  */
                LoginUser loginUser = new LoginUser();
                BeanUtils.copyProperties(userDO, loginUser);
                //生成的token是通过公钥和request中的ip进行绑定过的
                String token = JwtUtil.geneJsonWebToken(loginUser,request);


                return JsonData.buildSuccess(token);
            }else {
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_PWD_ERROR);
            }
        }else {
            //未注册
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNREGISTER);
        }

    }


}
