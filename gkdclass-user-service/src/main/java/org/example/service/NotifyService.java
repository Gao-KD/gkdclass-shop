package org.example.service;

import org.example.enums.SendCodeEnum;
import org.example.utils.JsonData;

public interface NotifyService {
    /**
     * 发送验证码
     */
    JsonData sendCode(SendCodeEnum sendCodeEnum,String to);

    /**
     * 验证验证码
     */
    boolean checkCode(SendCodeEnum sendCodeEnum,String to,String code);
}
