package org.example.service.impl;

import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.CacheKey;
import org.example.enums.BizCodeEnum;
import org.example.enums.SendCodeEnum;
import org.example.service.MailService;
import org.example.service.NotifyService;
import org.example.utils.CheckUtil;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {
    @Autowired
    private MailService mailService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 验证码标题
     */
    private static final String SUBJECT = "【在线教育平台验证码】";
    /***
     * 验证码内容
     */
    private static final String CONTENT = "验证码：%s，60s内有效，为了保障您的账户安全，请勿向他人泄漏验证码信息";

    /**
     * 过期时间
     */
    private static final long CODE_EXPIRED = 60 * 1000 * 10;

    /**
     * 前置：判断是否重复发送
     *
     * 1、存储验证码到缓存
     *
     * 2、发送验证码
     *
     * 后置：存储发送记录
     * @param sendCodeEnum
     * @param to
     * @return
     */
    @Override
    public JsonData sendCode(SendCodeEnum sendCodeEnum, String to) {
        /**
         * code:%s:%s
         * 第一个是类型，第二个是接收对象
         */
        String cacheKey = String.format(CacheKey.CHECK_CODE_KEY,sendCodeEnum.name(),to);
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);

        //如果为空 表示还未注册，否则判断60s内重复发送
        if (!StringUtils.isNullOrEmpty(cacheValue)){
            //字符串分割
            long ttl = Long.parseLong( cacheValue.split("_")[1]);
            //当前时间戳 - 验证码发送时间戳 < 60s 就不给发送
            if (System.currentTimeMillis() - ttl < 60 * 1000 ){
                log.info("重复发送验证码,时间间隔:" + ((System.currentTimeMillis() - ttl)/1000)+"秒");
                return JsonData.buildResult(BizCodeEnum.CODE_LIMITED);
            }
        }

        //获取6位验证码
        String code = CommonUtil.getRandomCode(6);
        //拼接时间戳(在commonUtil中)
        String value = code+"_"+System.currentTimeMillis();

        //存储到缓存中，并且设置过期时间：10min
        redisTemplate.opsForValue().set(cacheKey,value,CODE_EXPIRED, TimeUnit.MILLISECONDS);

        if (CheckUtil.isEmail(to)){//邮箱验证


            /**
             * 发送
             */
            mailService.sendMail(to, SUBJECT, String.format(CONTENT, code));

            return JsonData.buildSuccess(code);
        }else if (CheckUtil.isPhone(to)){//手机号验证
            return null;
        }else
            return JsonData.buildResult(BizCodeEnum.CODE_TO_ERROR);
    }
}
