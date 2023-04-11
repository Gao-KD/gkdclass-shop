package org.example.fegin;


import io.swagger.annotations.ApiParam;
import org.example.request.NewUserCouponRequest;
import org.example.utils.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gkdclass-coupon-service")
public interface CouponFeginService {

    @PostMapping("/api/coupon/v1/new_user_coupon")
    JsonData addNewUserCoupon(@ApiParam("用户对象")@RequestBody NewUserCouponRequest newUserCouponRequest);
}
