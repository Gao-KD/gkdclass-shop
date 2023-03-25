package org.example.controller;

import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.BizCodeEnum;
import org.example.enums.SendCodeEnum;
import org.example.service.NotifyService;
import org.example.utils.CommonUtil;
import org.example.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


@Api(tags = "通知模块")
@RestController
@RequestMapping("/api/user/v1")
@Slf4j
public class NotifyController {

    //对象名称对应KaptchaConfig中的@Qualifier("KaptchaProducer")
    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private NotifyService notifyService;

    //10min
    private static final long KAPTCHA_CODE_EXPIRED = 60 * 1000 * 10;

    @GetMapping("kaptcha")
    public void getKaptcha(HttpServletRequest request, HttpServletResponse response){
        String kaptchaText = kaptchaProducer.createText();
        log.info("图形验证码:"+kaptchaText);
        /**
         * 存储到redis
         */
        stringRedisTemplate.opsForValue().set(getKaptchaKey(request), kaptchaText,KAPTCHA_CODE_EXPIRED, TimeUnit.MILLISECONDS);


        BufferedImage image = kaptchaProducer.createImage(kaptchaText);
        ServletOutputStream outputStream = null;
        try{
            outputStream = response.getOutputStream();
            ImageIO.write(image,"jpg",outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            log.error("获取图形验证码异常:",e);
        }
    }

    @ApiOperation("发送邮箱注册验证码")
    @GetMapping("send_code")
    public JsonData sendRegisterCode(@RequestParam(value = "to",required = true)String to,
                                     @RequestParam(value = "kaptcha",required = true) String kaptcha,
                                     HttpServletRequest request){
        //获取ip+userAgent封装后的key值
        String key = getKaptchaKey(request);
        //去redis缓存中获取这个key
        String cacheKaptcha = stringRedisTemplate.opsForValue().get(key);

        /**
         * 匹配图形验证码
         */
        if (cacheKaptcha != null && kaptcha != null && kaptcha.equalsIgnoreCase(cacheKaptcha)){
            //成功,删除缓存
            stringRedisTemplate.delete(key);
            //发送邮箱验证码(枚举SendCodeEnum.USER_REGISTER)
            JsonData jsonData = notifyService.sendCode(SendCodeEnum.USER_REGISTER,to);
            log.info("邮箱验证码是:"+jsonData.getData());
            return JsonData.buildSuccess();

        }else {
            //失败
            return JsonData.buildResult(BizCodeEnum.CODE_KAPTCHA_ERROR);
        }
    }

    /**
     * 获取缓存的key
     * @param request
     * @return
     */
    private String getKaptchaKey(HttpServletRequest request){
        String ip = CommonUtil.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");

        String key = "user-service:kaptcha:"+CommonUtil.MD5(ip+userAgent);

        log.info("ip:"+ip);
        log.info("userAgent:"+userAgent);
        log.info("key:"+key);

        return key;
    }
}
