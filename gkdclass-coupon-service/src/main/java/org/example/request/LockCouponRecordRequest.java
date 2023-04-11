package org.example.request;

import lombok.Data;

import java.util.List;

@Data
public class LockCouponRecordRequest {
    /**
     * 优惠券记录表
     */
    private List<Long> lockCouponRecordIds;
    /**
     * 订单号
     */
    private String orderOutTradeNo;
}
