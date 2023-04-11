package org.example.service;

import org.example.enums.CouponCategoryEnum;
import org.example.request.NewUserCouponRequest;
import org.example.utils.JsonData;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author gaokd
 * @since 2023-03-28
 */
public interface CouponService {
    Map<String,Object> pageCouponActivity(int page,int size);


    /**
     * 领取优惠券接口
     * @param couponId
     * @param promotion
     * @return
     */
    JsonData receiveCoupon(long couponId, CouponCategoryEnum promotion);

    /**
     * 新用户注册发放优惠券
     * @param newUserCouponRequest
     * @return
     */
    JsonData initNewUserCoupon(NewUserCouponRequest newUserCouponRequest);
}
