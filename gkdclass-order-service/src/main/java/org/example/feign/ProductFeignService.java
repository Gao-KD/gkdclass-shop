package org.example.feign;

import org.example.utils.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "gkdclass-product-service")
public interface ProductFeignService {
    /**
     *  获取购物车最新价格（清空对应购买项）
     * @param productIdList
     * @return
     */
    @PostMapping("/api/cart/v1/confirm_order_cart_items")
    JsonData ConfirmOrderCartItem(@RequestBody List<Long> productIdList);
}
