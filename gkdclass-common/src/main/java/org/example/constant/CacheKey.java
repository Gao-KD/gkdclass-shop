package org.example.constant;

public class CacheKey {
    /**
     * 注册验证码，第一个是类型，第二个是接收号码
     */
    public static final String CHECK_CODE_KEY = "code:%s:%s";

    /**
     * 购物车，key是用户购物车唯一标识
     */

    public static final String CART_KEY = "cart:%s";
}
