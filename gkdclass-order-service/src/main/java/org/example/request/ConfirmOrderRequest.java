package org.example.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ConfirmOrderRequest {

    /**
     * 优惠券id
     * 传入null或者小于0，就不用购物券
     */
    @JsonProperty("coupon_record_id")
    private Long couponRecordId;

    /**
     * 商品id列表
     */
    @JsonProperty("product_ids")
    private List<Long> productIdList;

    /**
     * 支付方式
     */
    @JsonProperty("pay_type")
    private String payType;

    /**
     * 端类型
     */
    @JsonProperty("client_type")
    private String clientType;

    /**
     * 收货地址
     */
    @JsonProperty("address_id")
    private Long addressId;

    /**
     * 原价
     */
    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    /**
     * 折扣价
     */
    @JsonProperty("real_pay_price")
    private BigDecimal realPayPrice;
}
