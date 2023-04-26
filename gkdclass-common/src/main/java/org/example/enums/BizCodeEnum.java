package org.example.enums;

import lombok.Getter;

public enum BizCodeEnum {
    /**
     * 通用操作码
     */
    OPS_REPEAT(110001,"重复操作"),

    /**
     *验证码
     */
    CODE_TO_ERROR(240001,"接收号码不合规"),
    CODE_LIMITED(240002,"验证码发送过快"),
    CODE_ERROR(240003,"验证码错误"),
    CODE_KAPTCHA_ERROR(240101,"图形验证码错误"),

    /**
     * 账号
     */
    ACCOUNT_REPEAT(250001,"账号已经存在"),
    ACCOUNT_UNREGISTER(250002,"账号不存在"),
    ACCOUNT_PWD_ERROR(250003,"账号或者密码错误"),
    ACCOUNT_UNLOGIN(250004, "账号未登陆"),

    /**
     * 优惠券
     */

    COUPON_NO_EXIST(260001,"优惠券不存在"),
    COUPON_NO_STOCK(260002,"库存不足"),
    COUPON_OUT_OF_TIME(260003,"不在领取范围内"),
    COUPON_OUT_OF_LIMITE(2600004,"优惠券领取超过限制"),
    COUPON_STATE_ILLEGAL(260005,"优惠券状态不合法"),
    COUPON_RECORD_LOCK_FAIL(260006,"锁定优惠券失败" ),

    /**
     * 购物车
     */
    CART_FAIL(270001,"加入购物车失败"),
    CART_CHANGE_ILLEGAL(270002,"修改购物车不合法"),

    /**
     * 订单
     */
    ORDER_CONFIRM_NOT_EXIST(280001,"订单不存在" ),
    ORDER_CONFIRM_LOCK_PRODUCT_FAIL(280002, "锁定商品失败");


    @Getter
    private int code;

    @Getter
    private String message;

    private BizCodeEnum(int code,String message){
        this.code = code;
        this.message = message;
    }


}
