package org.example.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponRecordVO {

    /**
     * 优惠券id
     */
    @JsonProperty("coupon_id")
    private Long couponId;

    /**
     * 创建时间获得时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss",timezone = "GMT+8",locale = "zh")
    @JsonProperty("create_time")
    private Date createTime;

    /**
     * 使用状态  可用 NEW,已使用USED,过期 EXPIRED;
     */
    @JsonProperty("use_state")
    private String useState;

    /**
     * 用户id
     */
    @JsonProperty("user_id")
    private Long userId;

    /**
     * 用户昵称
     */
    @JsonProperty("user_name")
    private String userName;

    /**
     * 优惠券标题
     */
    @JsonProperty("coupon_title")
    private String couponTitle;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss",timezone = "GMT+8",locale = "zh")
    @JsonProperty("start_time")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss",timezone = "GMT+8",locale = "zh")
    @JsonProperty("end_time")
    private Date endTime;

    /**
     * 订单id
     */
    @JsonProperty("order_id")
    private Long orderId;

    /**
     * 抵扣价格
     */
    private BigDecimal price;

    /**
     * 满多少才可以使用
     */
    @JsonProperty("condition_price")
    private BigDecimal conditionPrice;

}
